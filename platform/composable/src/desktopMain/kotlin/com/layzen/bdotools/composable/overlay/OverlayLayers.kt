package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Порядок окон поверх игры. Оба окна «поверх всех», и выше то, что показали последним, поэтому окно оверлея
 * после каждого появления отмечается здесь, а окно кнопки меню в ответ поднимается над ним.
 */
internal object OverlayLayers {
    /** Сколько раз окно оверлея появлялось на экране. */
    var overlayShown by mutableIntStateOf(0)
}
