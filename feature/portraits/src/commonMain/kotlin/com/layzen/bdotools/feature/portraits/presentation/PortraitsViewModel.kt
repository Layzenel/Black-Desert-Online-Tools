package com.layzen.bdotools.feature.portraits.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.portraits.data.FolderAccess
import com.layzen.bdotools.feature.portraits.data.ImageSource
import com.layzen.bdotools.feature.portraits.data.PortraitsRepository
import com.layzen.bdotools.feature.portraits.data.PortraitsStorage
import com.layzen.bdotools.feature.portraits.presentation.controller.DecorationController
import com.layzen.bdotools.feature.portraits.presentation.controller.FavoritesController
import com.layzen.bdotools.feature.portraits.presentation.controller.ReorderController
import com.layzen.bdotools.feature.portraits.presentation.controller.ReplacementController
import com.layzen.bdotools.feature.portraits.presentation.controller.RestoreController
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.render.PortraitImages
import com.layzen.bdotools.feature.portraits.presentation.state.LoadProgress
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitItem
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import kotlinx.coroutines.flow.StateFlow

/**
 * Страница портретов. Сама отвечает только за доступ к папке и загрузку портретов,
 * а замену, украшения и резервные копии раздаёт контроллерам.
 */
class PortraitsViewModel(
    private val storage: PortraitsStorage,
    imageSource: ImageSource,
    repository: PortraitsRepository,
) : ViewModel() {

    private val context = PortraitsContext(viewModelScope, repository, PortraitImages())
    private val replacement = ReplacementController(context, imageSource)
    private val decoration = DecorationController(context)
    private val restore = RestoreController(context)
    private val favorites = FavoritesController(context)
    private val reorder = ReorderController(context)

    val state: StateFlow<PortraitsState> = context.state

    init {
        context.launch {
            val access = storage.access()
            context.update { it.copy(access = access) }
            if (access is FolderAccess.Granted) loadPortraits()
        }
    }

    fun onEvent(event: PortraitsEvent) {
        when (event) {
            PortraitsEvent.SelectFolder -> context.launch { onAccessGranted(storage.selectFolder()) }
            PortraitsEvent.RequestPermission -> context.launch { onAccessGranted(storage.requestPermission()) }
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
        }
    }

    private suspend fun onAccessGranted(access: FolderAccess.Granted) {
        context.update { it.copy(access = access, notice = null) }
        loadPortraits()
    }

    /**
     * Сразу показывает сетку из скелетонов в порядке окна выбора персонажа, а затем читает портреты
     * по одному и подставляет их по мере готовности. Пока чтение идёт, кнопки и действия недоступны.
     */
    private suspend fun loadPortraits() {
        val repository = context.repository
        context.update { it.copy(loading = LoadProgress(), reorder = null) }
        try {
            val ids = repository.portraitOrder()
            context.update { it.copy(portraits = ids.map { id -> PortraitItem(id, null) }, loading = LoadProgress(0, ids.size)) }
            ids.forEachIndexed { index, id ->
                val image = context.images.decode(repository.readPortrait(id))
                context.update { it.withPortraitImages(mapOf(id to image)).copy(loading = LoadProgress(index + 1, ids.size)) }
                context.breathe()
            }
            val decorations = repository.loadDecorations()
            val backups = repository.backups()
            context.update { it.copy(decorations = decorations, backups = backups) }
        } finally {
            context.update { it.copy(loading = null) }
        }
    }
}
