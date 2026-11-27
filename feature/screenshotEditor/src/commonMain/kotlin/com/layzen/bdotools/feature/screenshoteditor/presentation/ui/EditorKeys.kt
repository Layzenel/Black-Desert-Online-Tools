package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.layzen.bdotools.composable.input.handleHistoryKeys
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent

/**
 * Клавиши холста: Ctrl+Z / Ctrl+Y — история, Esc — отменить обрезку и снять выбор, Delete и Backspace — удалить
 * выбранную правку, Enter — подтвердить обрезку.
 */
internal fun KeyEvent.handleEditorKeys(onEvent: (ScreenshotEditorEvent) -> Unit): Boolean {
    if (handleHistoryKeys({ onEvent(ScreenshotEditorEvent.Undo) }, { onEvent(ScreenshotEditorEvent.Redo) })) return true
    val event = when (key) {
        Key.Escape -> ScreenshotEditorEvent.Cancel
        Key.Delete, Key.Backspace -> ScreenshotEditorEvent.DeleteSelected
        Key.Enter, Key.NumPadEnter -> ScreenshotEditorEvent.ConfirmCrop
        else -> return false
    }
    if (type == KeyEventType.KeyDown) onEvent(event)
    return true
}
