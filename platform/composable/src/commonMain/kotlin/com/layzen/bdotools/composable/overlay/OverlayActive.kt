package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Включён ли оверлей: правый клик по кнопке меню переключает его. В инактиве окна поверх игры скрыты
 * ([rememberOverlayShown]), а кнопка меню остаётся — ею оверлей включают обратно.
 */
internal object OverlayActive {
    /** Окна оверлея показываются. */
    var value by mutableStateOf(true)
}
