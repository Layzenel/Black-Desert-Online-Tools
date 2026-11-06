package com.layzen.bdotools.feature.portraitconstructor.presentation.event

import com.layzen.bdotools.feature.portraitconstructor.domain.SlotIcon
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget

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
    /** Включает или выключает ячейку маски с номером [index]. */
    data class ToggleMaskCell(val index: Int) : PortraitsEvent
    data object CloseEditor : PortraitsEvent
    data object Apply : PortraitsEvent

    data class OpenDecoration(val characterId: String) : PortraitsEvent
    data class CaptionChanged(val text: String) : PortraitsEvent
    data class BadgeChanged(val icon: SlotIcon?) : PortraitsEvent
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

    data object StartReorder : PortraitsEvent
    data class PickReorderPortrait(val characterId: String) : PortraitsEvent
    data object CancelReorder : PortraitsEvent

    data object OpenFavorites : PortraitsEvent
    data class SelectFavorite(val name: String) : PortraitsEvent
    data object SaveFavorites : PortraitsEvent
    data object ApplyFavorite : PortraitsEvent
    data object CloseFavorites : PortraitsEvent
    data class FavoriteTitleChanged(val text: String) : PortraitsEvent
    data object RenameFavorite : PortraitsEvent
    data class ToggleFavoriteMark(val name: String) : PortraitsEvent
    data object RequestDeleteFavorites : PortraitsEvent
    data object CancelDeleteFavorites : PortraitsEvent
    data object ConfirmDeleteFavorites : PortraitsEvent
    data object RequestReplaceFavorite : PortraitsEvent
    data object CancelReplaceFavorite : PortraitsEvent
    data object ConfirmReplaceFavorite : PortraitsEvent

    /** Открывает окно синхронизации или проверяет заново заметки и то, закрыта ли игра. */
    data object OpenNotesSync : PortraitsEvent
    data class ConfirmGameClosed(val checked: Boolean) : PortraitsEvent
    data object CloseNotesSync : PortraitsEvent
    data object ApplyNotesSync : PortraitsEvent
}
