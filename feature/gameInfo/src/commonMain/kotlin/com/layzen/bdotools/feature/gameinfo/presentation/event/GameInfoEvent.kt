package com.layzen.bdotools.feature.gameinfo.presentation.event

import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep

/** Действия игрока во вкладке «Информация» меню оверлея. */
sealed interface GameInfoEvent {
    data object TogglePing : GameInfoEvent

    /** Показ FPS; без прав администратора не меняется. */
    data object ToggleFps : GameInfoEvent

    /** Вид FPS: компактно или подробно. */
    data class SetFpsStyle(val style: FpsStyle) : GameInfoEvent

    /** Сетка на весь экран игры: показать или спрятать. */
    data object ToggleGrid : GameInfoEvent

    /** Шаг линий сетки. */
    data class SetGridStep(val step: GridStep) : GameInfoEvent

    /** Непрозрачность сетки в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetGridOpacity(val percent: Int, val save: Boolean) : GameInfoEvent

    /** Размер пинга в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetScale(val percent: Int, val save: Boolean) : GameInfoEvent

    /** Непрозрачность пинга в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetOpacity(val percent: Int, val save: Boolean) : GameInfoEvent
}
