package com.layzen.bdotools.feature.portraits.presentation.event

import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget

/** Действия пользователя на странице портретов. */
sealed interface PortraitsEvent {
    data object SelectFolder : PortraitsEvent
    data object RequestPermission : PortraitsEvent
    data object Reload : PortraitsEvent
    data object DismissNotice : PortraitsEvent

    /** Отрисовщик украшений живёт в композиции (ему нужны шрифты и SVG) и передаётся сюда при каждом изменении. */
    data class DecoratorChanged(val decorator: PortraitDecorator) : PortraitsEvent

    data class OpenSource(val target: ReplaceTarget) : PortraitsEvent
    data object CloseSource : PortraitsEvent
    data class UrlChanged(val url: String) : PortraitsEvent
    data object PickFromDevice : PortraitsEvent
    data object LoadFromUrl : PortraitsEvent

    /** Сдвиг в пикселях маски. */
    data class MoveImage(val dx: Float, val dy: Float) : PortraitsEvent
    data class ZoomImage(val scale: Float) : PortraitsEvent
    data class RotateImage(val degrees: Float) : PortraitsEvent
    data object ResetPlacement : PortraitsEvent
    data object CloseEditor : PortraitsEvent
    data object Apply : PortraitsEvent

    data class OpenDecoration(val characterId: String) : PortraitsEvent
    data class CaptionChanged(val text: String) : PortraitsEvent
    data class BadgeChanged(val slot: BadgeSlot, val icon: BadgeIcon?) : PortraitsEvent
    data object CloseDecoration : PortraitsEvent
    data object ApplyDecoration : PortraitsEvent

    data class OpenRestore(val target: ReplaceTarget) : PortraitsEvent
    data class SelectBackup(val name: String) : PortraitsEvent
    data class ToggleRestorePortrait(val characterId: String) : PortraitsEvent
    data class CheckAllRestorePortraits(val checked: Boolean) : PortraitsEvent
    data object CloseRestore : PortraitsEvent
    data object ApplyRestore : PortraitsEvent

    data class ToggleBackupMark(val name: String) : PortraitsEvent
    data object RequestDeleteBackups : PortraitsEvent
    data object CancelDeleteBackups : PortraitsEvent
    data object ConfirmDeleteBackups : PortraitsEvent
}
