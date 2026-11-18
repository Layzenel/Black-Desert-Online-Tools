package com.layzen.bdotools.feature.gameinfo.presentation.event

/** Действия игрока во вкладке «Информация» меню оверлея. */
sealed interface GameInfoEvent {
    data object TogglePing : GameInfoEvent
    data object ToggleMove : GameInfoEvent

    /** Размер пинга в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetScale(val percent: Int, val save: Boolean) : GameInfoEvent

    /** Непрозрачность пинга в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetOpacity(val percent: Int, val save: Boolean) : GameInfoEvent
}
