package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Горячие клавиши истории: Ctrl+Z — [onUndo], Ctrl+Shift+Z и Ctrl+Y — [onRedo]. На Mac вместо Ctrl — Cmd.
 * Возвращает `true`, если клавиша — одна из них: тогда её не нужно обрабатывать дальше.
 */
internal fun KeyEvent.handleHistoryKeys(onUndo: () -> Unit, onRedo: () -> Unit): Boolean {
    if (!isCtrlPressed && !isMetaPressed) return false
    val action = when {
        key == Key.Z && isShiftPressed -> onRedo
        key == Key.Z -> onUndo
        key == Key.Y -> onRedo
        else -> return false
    }
    if (type == KeyEventType.KeyDown) action()
    return true
}
