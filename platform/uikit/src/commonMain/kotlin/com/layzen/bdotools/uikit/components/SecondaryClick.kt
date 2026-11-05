package com.layzen.bdotools.uikit.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Правая кнопка мыши: [onClick] получает точку нажатия в координатах элемента. Нажатие поглощается до дочерних
 * элементов, поэтому их жесты (выбор, перетаскивание, сдвиг холста) правым кликом не запускаются.
 */
fun Modifier.onSecondaryClick(onClick: (Offset) -> Unit): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type != PointerEventType.Press || !event.buttons.isSecondaryPressed) continue
            val change = event.changes.first()
            change.consume()
            onClick(change.position)
        }
    }
}
