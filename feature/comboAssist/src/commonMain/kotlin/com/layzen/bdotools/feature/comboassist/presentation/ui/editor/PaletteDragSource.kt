package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalViewConfiguration

/**
 * Строку списка с умением [skillNo] можно потянуть на холст: положение курсора уходит в [drag]. Клик по строке
 * остаётся кликом. Жест ловится на первом проходе событий ([PointerEventPass.Initial]) — раньше клика строки,
 * подсказки умения и прокрутки списка, которые иначе забирают его себе; начатое перетаскивание поглощает события,
 * чтобы отпускание не засчиталось кликом и список не прокрутился.
 */
internal fun Modifier.paletteDragSource(skillNo: Int, drag: PaletteDrag): Modifier = composed {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val slop = LocalViewConfiguration.current.touchSlop
    onGloballyPositioned { origin = it.positionInRoot() }.pointerInput(skillNo) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var dragging = false
            while (true) {
                val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) {
                    if (dragging) {
                        change.consume()
                        drag.release()
                    }
                    break
                }
                if (!dragging && (change.position - down.position).getDistance() > slop) dragging = true
                if (dragging) {
                    change.consume()
                    drag.move(skillNo, origin + change.position)
                }
            }
        }
    }
}
