package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/**
 * Новое состояние после [event], которое не ходит в файлы и не тянется мышью; `null` — событие обрабатывает
 * ViewModel сама (открытие, сохранение, перетаскивание).
 */
internal fun ScreenshotEditorState.reduce(event: ScreenshotEditorEvent): ScreenshotEditorState? = when (event) {
    ScreenshotEditorEvent.Undo -> copy(history = history.undo(), draft = null)
    ScreenshotEditorEvent.Redo -> copy(history = history.redo(), draft = null)
    ScreenshotEditorEvent.ResetCrop -> resetCrop()
    ScreenshotEditorEvent.Close -> copy(image = null, name = "", history = EditHistory(), draft = null, comparing = false)
    is ScreenshotEditorEvent.SelectTool -> copy(tool = event.tool, comparing = false)
    is ScreenshotEditorEvent.SelectColor -> copy(colorIndex = event.index)
    is ScreenshotEditorEvent.Thickness -> copy(thickness = event.value.coerceIn(1, MAX_THICKNESS))
    is ScreenshotEditorEvent.Text -> copy(text = event.value)
    is ScreenshotEditorEvent.Aspect -> copy(aspect = event.aspect)
    is ScreenshotEditorEvent.Scale -> copy(scale = event.scale)
    is ScreenshotEditorEvent.Format -> copy(format = event.format)
    is ScreenshotEditorEvent.Tap -> labelAt(this, event.at)?.let { copy(history = history.add(it)) } ?: this
    is ScreenshotEditorEvent.Adjust -> copy(draft = Edit.Adjust(event.adjustments), comparing = false)
    ScreenshotEditorEvent.AdjustFinished -> finishAdjust()
    is ScreenshotEditorEvent.Preset -> withEdit(Edit.Adjust(event.preset.adjustments))
    ScreenshotEditorEvent.Rotate -> withEdit(Edit.Orient(history.orientation.rotated()))
    ScreenshotEditorEvent.Flip -> withEdit(Edit.Orient(history.orientation.flipped()))
    ScreenshotEditorEvent.ToggleCompare -> copy(comparing = !comparing)
    ScreenshotEditorEvent.DismissNotice -> copy(notice = null)
    else -> null
}

private fun ScreenshotEditorState.withEdit(edit: Edit) = copy(history = history.add(edit), draft = null, comparing = false)

/** Отпустили ползунок: ретушь из черновика в историю, если она изменилась. */
private fun ScreenshotEditorState.finishAdjust(): ScreenshotEditorState {
    val draft = draft as? Edit.Adjust ?: return this
    return if (draft.adjustments == history.adjustments) copy(draft = null) else withEdit(draft)
}

/** Обрезка сбрасывается новой правкой «вся картинка» — так сброс тоже отменяется. */
private fun ScreenshotEditorState.resetCrop(): ScreenshotEditorState {
    val image = image ?: return this
    if (history.crop == null) return this
    return withEdit(Edit.Crop(ImageRect(0f, 0f, image.width.toFloat(), image.height.toFloat())))
}
