package com.layzen.bdotools.feature.portraitconstructor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.BackupsInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.favorite.FavoritesInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemosInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsInteractor
import com.layzen.bdotools.feature.portraitconstructor.presentation.controller.DecorationController
import com.layzen.bdotools.feature.portraitconstructor.presentation.controller.FavoritesController
import com.layzen.bdotools.feature.portraitconstructor.presentation.controller.NotesSyncController
import com.layzen.bdotools.feature.portraitconstructor.presentation.controller.ReorderController
import com.layzen.bdotools.feature.portraitconstructor.presentation.controller.ReplacementController
import com.layzen.bdotools.feature.portraitconstructor.presentation.controller.RestoreController
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitImages
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.LoadProgress
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitItem
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Страница портретов. Сама отвечает только за доступ к папке и загрузку портретов,
 * а замену, украшения и резервные копии раздаёт контроллерам.
 */
class PortraitsViewModel(
    private val portraits: PortraitsInteractor,
    backups: BackupsInteractor,
    favorites: FavoritesInteractor,
    memos: MemosInteractor,
    private val accounts: GameAccounts,
) : ViewModel() {

    private val context = PortraitsContext(viewModelScope, portraits, backups, favorites, PortraitImages())
    private val replacement = ReplacementController(context)
    private val decoration = DecorationController(context)
    private val restore = RestoreController(context)
    private val favorites = FavoritesController(context)
    private val reorder = ReorderController(context)
    private val notesSync = NotesSyncController(context, memos)

    /** Состояние страницы для экрана; меняется только контроллерами. */
    val state: StateFlow<PortraitsState> = context.state

    init {
        context.launch {
            val access = portraits.access()
            context.update { it.copy(access = access) }
            if (access is FolderAccess.Granted) loadPortraits()
        }
        viewModelScope.launch {
            accounts.state.map { it.selectedId }.distinctUntilChanged().drop(1).collect {
                if (context.current.access is FolderAccess.Granted) context.launch { loadPortraits() }
            }
        }
    }

    /** Единая точка входа для действий пользователя: раздаёт [event] контроллерам и загрузке портретов. */
    fun onEvent(event: PortraitsEvent) {
        when (event) {
            PortraitsEvent.SelectFolder -> context.launch { onAccessGranted(portraits.selectFolder()) }
            PortraitsEvent.RequestPermission -> context.launch { onAccessGranted(portraits.requestPermission()) }
            PortraitsEvent.Reload -> context.launch { loadPortraits() }
            PortraitsEvent.DismissNotice -> context.update { it.copy(notice = null) }
            is PortraitsEvent.DecoratorChanged -> context.images.useDecorator(event.decorator)

            is PortraitsEvent.OpenSource -> replacement.openSource(event.target)
            PortraitsEvent.CloseSource -> replacement.closeSource()
            is PortraitsEvent.UrlChanged -> replacement.changeUrl(event.url)
            PortraitsEvent.PickFromDevice -> replacement.pickFromDevice()
            PortraitsEvent.LoadFromUrl -> replacement.loadFromUrl()
            is PortraitsEvent.MoveImage -> replacement.updatePlacement { it.movedBy(event.dx, event.dy) }
            is PortraitsEvent.ZoomImage -> replacement.updatePlacement { it.zoomedTo(event.scale) }
            is PortraitsEvent.RotateImage -> replacement.updatePlacement { it.rotatedTo(event.degrees) }
            PortraitsEvent.ResetPlacement -> replacement.resetPlacement()
            is PortraitsEvent.ToggleMaskCell -> replacement.toggleCell(event.index)
            PortraitsEvent.CloseEditor -> replacement.closeEditor()
            PortraitsEvent.Apply -> replacement.apply()

            is PortraitsEvent.OpenDecoration -> decoration.open(event.characterId)
            is PortraitsEvent.CaptionChanged -> decoration.changeCaption(event.text)
            is PortraitsEvent.BadgeChanged -> decoration.changeBadge(event.icon)
            PortraitsEvent.CloseDecoration -> decoration.close()
            PortraitsEvent.ApplyDecoration -> decoration.apply()

            is PortraitsEvent.OpenRestore -> restore.open(event.target)
            is PortraitsEvent.SelectBackup -> restore.select(event.name)
            is PortraitsEvent.ToggleRestorePortrait -> restore.togglePortrait(event.characterId)
            is PortraitsEvent.CheckAllRestorePortraits -> restore.checkAll(event.checked)
            PortraitsEvent.CloseRestore -> restore.close()
            PortraitsEvent.ApplyRestore -> restore.apply()
            is PortraitsEvent.ToggleBackupMark -> restore.toggleBackupMark(event.name)
            PortraitsEvent.RequestDeleteBackups -> restore.requestDelete()
            PortraitsEvent.CancelDeleteBackups -> restore.cancelDelete()
            PortraitsEvent.ConfirmDeleteBackups -> restore.deleteMarked()
            PortraitsEvent.StartReorder -> reorder.start()
            is PortraitsEvent.PickReorderPortrait -> reorder.pick(event.characterId)
            PortraitsEvent.CancelReorder -> reorder.cancel()
            PortraitsEvent.OpenFavorites -> favorites.open()
            is PortraitsEvent.SelectFavorite -> favorites.select(event.name)
            PortraitsEvent.SaveFavorites -> favorites.saveAll()
            PortraitsEvent.ApplyFavorite -> favorites.apply()
            PortraitsEvent.CloseFavorites -> favorites.close()
            is PortraitsEvent.FavoriteTitleChanged -> favorites.changeTitle(event.text)
            PortraitsEvent.RenameFavorite -> favorites.rename()
            is PortraitsEvent.ToggleFavoriteMark -> favorites.toggleMark(event.name)
            PortraitsEvent.RequestDeleteFavorites -> favorites.requestDelete()
            PortraitsEvent.CancelDeleteFavorites -> favorites.cancelDelete()
            PortraitsEvent.ConfirmDeleteFavorites -> favorites.deleteMarked()
            PortraitsEvent.RequestReplaceFavorite -> favorites.requestReplace()
            PortraitsEvent.CancelReplaceFavorite -> favorites.cancelReplace()
            PortraitsEvent.ConfirmReplaceFavorite -> favorites.replaceSelected()
            PortraitsEvent.OpenNotesSync -> notesSync.open()
            is PortraitsEvent.ConfirmGameClosed -> notesSync.confirmGameClosed(event.checked)
            PortraitsEvent.CloseNotesSync -> notesSync.close()
            PortraitsEvent.ApplyNotesSync -> notesSync.apply()
        }
    }

    private suspend fun onAccessGranted(access: FolderAccess.Granted) {
        context.update { it.copy(access = access, notice = null) }
        loadPortraits()
    }

    /**
     * Сразу показывает сетку из скелетонов в порядке окна выбора персонажа, а затем читает портреты
     * по одному и подставляет их по мере готовности. Пока чтение идёт, кнопки и действия недоступны.
     * Портреты только выбранного аккаунта; пока аккаунт не выбран в окне выбора, сетка пустая.
     */
    private suspend fun loadPortraits() {
        context.update { it.copy(loading = LoadProgress(), reorder = null) }
        try {
            accounts.refresh()
            val choice = accounts.state.value
            val ids = if (choice.needsChoice) emptyList() else portraits.portraitOrder(choice.selected)
            portraits.useAccount(choice.selected)
            context.update {
                it.copy(
                    portraits = ids.map { id -> PortraitItem(id, null) },
                    accountId = choice.selected?.id,
                    loading = LoadProgress(0, ids.size),
                )
            }
            ids.forEachIndexed { index, id ->
                val image = context.images.decode(portraits.readPortrait(id))
                context.update { it.withPortraitImages(mapOf(id to image)).copy(loading = LoadProgress(index + 1, ids.size)) }
                context.breathe()
            }
            val decorations = portraits.loadDecorations()
            // пока аккаунт не выбран, копий нет: в корне папки копий лежат папки аккаунтов, а не копии
            val backups = if (choice.needsChoice) emptyList() else context.backups.backups()
            context.update { it.copy(decorations = decorations, backups = backups) }
        } finally {
            context.update { it.copy(loading = null) }
        }
    }
}
