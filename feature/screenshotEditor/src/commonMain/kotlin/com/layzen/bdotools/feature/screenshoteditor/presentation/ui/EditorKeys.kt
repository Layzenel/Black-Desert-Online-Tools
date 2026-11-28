package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.layzen.bdotools.composable.input.handleHistoryKeys
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent

/**
 * Клавиши холста: Ctrl+Z / Ctrl+Y — история, Esc — отменить обрезку и снять выбор, Delete и Backspace — удалить
 * выбранную правку, Enter — подтвердить обрезку. Ctrl+C копирует выбранную пометку ([hasSelection]), а без неё —
 * картинку с правками ([copyImage]); Ctrl+V вставляет пометку, Ctrl+D дублирует выбранную.
 */
internal fun KeyEvent.handleEditorKeys(onEvent: (ScreenshotEditorEvent) -> Unit, hasSelection: Boolean, copyImage: () -> Unit): Boolean {
    if (handleHistoryKeys({ onEvent(ScreenshotEditorEvent.Undo) }, { onEvent(ScreenshotEditorEvent.Redo) })) return true
    if (isCtrlPressed || isMetaPressed) {
        val action: () -> Unit = when (key) {
            Key.C -> if (hasSelection) { { onEvent(ScreenshotEditorEvent.CopyMark) } } else copyImage
            Key.V -> { { onEvent(ScreenshotEditorEvent.PasteMark) } }
            Key.D -> { { onEvent(ScreenshotEditorEvent.DuplicateMark) } }
            else -> return false
        }
        if (type == KeyEventType.KeyDown) action()
        return true
    }
    val event = when (key) {
        Key.Escape -> ScreenshotEditorEvent.Cancel
        Key.Delete, Key.Backspace -> ScreenshotEditorEvent.DeleteSelected
        Key.Enter, Key.NumPadEnter -> ScreenshotEditorEvent.ConfirmCrop
        else -> return false
    }
    if (type == KeyEventType.KeyDown) onEvent(event)
    return true
}

/** Клавиши, пока набирают надпись на холсте: Enter — поставить её, Esc — отменить; остальное — в поле ввода. */
internal fun KeyEvent.handleTextKeys(onEvent: (ScreenshotEditorEvent) -> Unit): Boolean {
    val event = when (key) {
        Key.Enter, Key.NumPadEnter -> ScreenshotEditorEvent.CommitText
        Key.Escape -> ScreenshotEditorEvent.Cancel
        else -> return false
    }
    if (type == KeyEventType.KeyDown) onEvent(event)
    return true
}
