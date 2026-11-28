package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ArrowStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.FrameShape
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.LabelStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.MAX_HIDE_STRENGTH
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.MarkStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedBy
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.scaled
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.styled
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import kotlin.math.max

// Вставленная и продублированная пометка встаёт со сдвигом — на пятидесятую длинной стороны, чтобы её было видно
private const val PASTE_SHIFT_PER_SIDE = 50f

/**
 * Новое состояние после [event], которое не ходит в файлы и не тянется мышью; `null` — событие обрабатывает
 * ViewModel сама (открытие, сохранение, перетаскивание).
 */
internal fun ScreenshotEditorState.reduce(event: ScreenshotEditorEvent): ScreenshotEditorState? = when (event) {
    ScreenshotEditorEvent.Undo -> withHistory(history.undo())
    ScreenshotEditorEvent.Redo -> withHistory(history.redo())
    ScreenshotEditorEvent.Discard -> withHistory(history.cleared())
    ScreenshotEditorEvent.Close -> copy(image = null, name = "", history = EditHistory(), saved = emptyList(), tool = null).idle()
    is ScreenshotEditorEvent.SelectTool -> withTypedLabel().copy(tool = event.tool, comparing = false).idle()
    is ScreenshotEditorEvent.SelectColor -> withColor(event.color)
    is ScreenshotEditorEvent.Thickness -> event.value.coerceIn(1, MAX_THICKNESS).let { value -> copy(thickness = value).restyled { withThickness(it, value) } }
    is ScreenshotEditorEvent.SelectStyle -> withStyle(event.style)
    is ScreenshotEditorEvent.Hide -> copy(hideMode = event.mode).restyled { (it as? Edit.Pixelate)?.copy(mode = event.mode) ?: it }
    is ScreenshotEditorEvent.HideStrength -> event.value.coerceIn(1, MAX_HIDE_STRENGTH).let { value ->
        copy(hideStrength = value).restyled { (it as? Edit.Pixelate)?.copy(strength = value) ?: it }
    }
    ScreenshotEditorEvent.StartPicking -> copy(picking = !picking)
    is ScreenshotEditorEvent.ContextPress -> idle().copy(selected = event.hit).withSelectionStyle()
    is ScreenshotEditorEvent.EditLabel -> editLabel(event.index)
    ScreenshotEditorEvent.CopyMark -> selectedEdit?.let { copy(copied = it) } ?: this
    ScreenshotEditorEvent.PasteMark -> copied?.let { mark -> shifted(mark).let { idle().withAdded(it).copy(copied = it) } } ?: this
    ScreenshotEditorEvent.DuplicateMark -> selectedEdit?.let { idle().withAdded(shifted(it)) } ?: this
    is ScreenshotEditorEvent.Text -> copy(text = event.value)
    is ScreenshotEditorEvent.Aspect -> copy(aspect = event.aspect)
    is ScreenshotEditorEvent.Scale -> copy(scale = event.scale)
    is ScreenshotEditorEvent.Format -> copy(format = event.format)
    ScreenshotEditorEvent.Cancel -> idle()
    ScreenshotEditorEvent.ConfirmCrop -> pendingCrop?.let { withEdit(Edit.Crop(it)) } ?: this
    ScreenshotEditorEvent.DeleteSelected -> selected?.let { withHistory(history.remove(it)) } ?: this
    ScreenshotEditorEvent.CommitText -> withTypedLabel()
    is ScreenshotEditorEvent.ScaleSelected -> restyled { it.scaled(event.factor, event.around) }
    is ScreenshotEditorEvent.Adjust -> copy(draft = Edit.Adjust(event.adjustments), comparing = false)
    ScreenshotEditorEvent.AdjustFinished -> finishAdjust()
    is ScreenshotEditorEvent.Preset -> withEdit(Edit.Adjust(event.preset.adjustments))
    ScreenshotEditorEvent.Rotate -> withEdit(Edit.Orient(history.orientation.rotated()))
    ScreenshotEditorEvent.Flip -> withEdit(Edit.Orient(history.orientation.flipped()))
    ScreenshotEditorEvent.ToggleCompare -> copy(comparing = !comparing).idle()
    ScreenshotEditorEvent.DismissNotice -> copy(notice = null)
    else -> null
}

/**
 * Без черновика, обведённой обрезки, выбора, поля надписи и пипетки: номер выбранной правки после смены истории
 * уже не тот.
 */
private fun ScreenshotEditorState.idle() = copy(
    draft = null,
    pendingCrop = null,
    selected = null,
    dragOffset = null,
    resized = null,
    textAt = null,
    text = "",
    editing = null,
    picking = false,
)

private fun ScreenshotEditorState.withHistory(next: EditHistory) = copy(history = next).idle()

private fun ScreenshotEditorState.withEdit(edit: Edit) = withHistory(history.add(edit)).copy(comparing = false)

/**
 * Вид [style] — для новых пометок его инструмента и для выбранной, если она того же рода; иначе выбор снимается,
 * как при смене инструмента.
 */
private fun ScreenshotEditorState.withStyle(style: MarkStyle): ScreenshotEditorState {
    val styledState = when (style) {
        is ArrowStyle -> copy(arrowStyle = style, tool = EditorTool.Arrow)
        is FrameShape -> copy(frameShape = style, tool = EditorTool.Frame)
        is LabelStyle -> copy(labelStyle = style, tool = EditorTool.Text)
    }.copy(comparing = false)
    val edit = selectedEdit
    return if (edit != null && edit.styled(style) != edit) styledState.restyled { it.styled(style) } else styledState.idle()
}

/** Надпись номер [index] — в поле ввода на холсте с её текстом, цветом и видом. */
private fun ScreenshotEditorState.editLabel(index: Int): ScreenshotEditorState {
    val label = history.edits.getOrNull(index) as? Edit.Label ?: return this
    return idle().copy(editing = index, textAt = label.at, text = label.text, color = label.color, labelStyle = label.style)
}

/** Пометка [edit], сдвинутая вправо вниз, чтобы копия не легла точно на оригинал. */
private fun ScreenshotEditorState.shifted(edit: Edit): Edit {
    val image = image ?: return edit
    val shift = max(image.width, image.height) / PASTE_SHIFT_PER_SIDE
    return edit.movedBy(shift, shift)
}

/** Отпустили ползунок: ретушь из черновика в историю, если она изменилась. */
private fun ScreenshotEditorState.finishAdjust(): ScreenshotEditorState {
    val draft = draft as? Edit.Adjust ?: return this
    return if (draft.adjustments == history.adjustments) copy(draft = null) else withEdit(draft)
}
