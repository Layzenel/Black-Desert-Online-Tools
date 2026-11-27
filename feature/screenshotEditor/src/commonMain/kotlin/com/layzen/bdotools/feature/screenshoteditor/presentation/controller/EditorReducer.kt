package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/**
 * Новое состояние после [event], которое не ходит в файлы и не тянется мышью; `null` — событие обрабатывает
 * ViewModel сама (открытие, сохранение, перетаскивание).
 */
internal fun ScreenshotEditorState.reduce(event: ScreenshotEditorEvent): ScreenshotEditorState? = when (event) {
    ScreenshotEditorEvent.Undo -> withHistory(history.undo())
    ScreenshotEditorEvent.Redo -> withHistory(history.redo())
    ScreenshotEditorEvent.Discard -> withHistory(history.cleared())
    ScreenshotEditorEvent.Close -> copy(image = null, name = "", history = EditHistory(), saved = emptyList()).idle()
    is ScreenshotEditorEvent.SelectTool -> copy(tool = event.tool, comparing = false).idle()
    is ScreenshotEditorEvent.SelectColor -> copy(colorIndex = event.index)
    is ScreenshotEditorEvent.Thickness -> copy(thickness = event.value.coerceIn(1, MAX_THICKNESS))
    is ScreenshotEditorEvent.Text -> copy(text = event.value)
    is ScreenshotEditorEvent.Aspect -> copy(aspect = event.aspect)
    is ScreenshotEditorEvent.Scale -> copy(scale = event.scale)
    is ScreenshotEditorEvent.Format -> copy(format = event.format)
    ScreenshotEditorEvent.Cancel -> idle()
    ScreenshotEditorEvent.ConfirmCrop -> pendingCrop?.let { withEdit(Edit.Crop(it)) } ?: this
    ScreenshotEditorEvent.DeleteSelected -> selected?.let { withHistory(history.remove(it)) } ?: this
    is ScreenshotEditorEvent.Adjust -> copy(draft = Edit.Adjust(event.adjustments), comparing = false)
    ScreenshotEditorEvent.AdjustFinished -> finishAdjust()
    is ScreenshotEditorEvent.Preset -> withEdit(Edit.Adjust(event.preset.adjustments))
    ScreenshotEditorEvent.Rotate -> withEdit(Edit.Orient(history.orientation.rotated()))
    ScreenshotEditorEvent.Flip -> withEdit(Edit.Orient(history.orientation.flipped()))
    ScreenshotEditorEvent.ToggleCompare -> copy(comparing = !comparing).idle()
    ScreenshotEditorEvent.DismissNotice -> copy(notice = null)
    else -> null
}

/** Без черновика, обведённой обрезки и выбора: номер выбранной правки после смены истории уже не тот. */
private fun ScreenshotEditorState.idle() = copy(draft = null, pendingCrop = null, selected = null, dragOffset = null)

private fun ScreenshotEditorState.withHistory(next: EditHistory) = copy(history = next).idle()

private fun ScreenshotEditorState.withEdit(edit: Edit) = withHistory(history.add(edit)).copy(comparing = false)

/** Отпустили ползунок: ретушь из черновика в историю, если она изменилась. */
private fun ScreenshotEditorState.finishAdjust(): ScreenshotEditorState {
    val draft = draft as? Edit.Adjust ?: return this
    return if (draft.adjustments == history.adjustments) copy(draft = null) else withEdit(draft)
}
