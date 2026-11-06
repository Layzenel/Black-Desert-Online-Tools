package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput

/** Значок-кнопка на рамке элемента: клик зовёт [onTap], нажатие поглощается, чтобы не схватить элемент под значком. */
@Composable
internal fun Modifier.consumingTap(onTap: () -> Unit): Modifier {
    val currentOnTap by rememberUpdatedState(onTap)
    return pointerHoverIcon(PointerIcon.Hand).pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown().consume()
            waitForUpOrCancellation()?.let {
                it.consume()
                currentOnTap()
            }
        }
    }
}
