package com.layzen.bdotools.feature.screenshoteditor.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.domain.file.GalleryInteractor
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap

/** Галерея папки `ScreenShot`: доступ к папке, список, миниатюры, проводник и корзина. */
class GalleryViewModel(private val interactor: GalleryInteractor) : ViewModel() {
    private val _state = MutableStateFlow(GalleryState(canManageFiles = interactor.canManageFiles))

    /** Состояние галереи. */
    val state: StateFlow<GalleryState> = _state.asStateFlow()

    // миниатюры, которые уже грузятся или загружены: плитка просит свою при каждом появлении на экране
    private val requested = mutableSetOf<String>()

    /** Обрабатывает действие в галерее. */
    fun onEvent(event: GalleryEvent) {
        when (event) {
            GalleryEvent.Refresh -> load { interactor.access() }
            GalleryEvent.SelectFolder -> load { interactor.selectFolder() }
            GalleryEvent.RequestPermission -> load { interactor.requestPermission() }
            is GalleryEvent.LoadThumbnail -> loadThumbnail(event.entry)
            is GalleryEvent.Reveal -> action { interactor.reveal(event.entry) }
            is GalleryEvent.Trash -> trash(event.entry)
            GalleryEvent.DismissNotice -> _state.update { it.copy(notice = null) }
        }
    }

    /** Узнаёт доступ к папке через [access] и, если он есть, перечитывает список. */
    private fun load(access: suspend () -> FolderAccess) = action {
        val granted = access()
        val entries = if (granted is FolderAccess.Granted) interactor.list() else emptyList()
        _state.update { it.copy(access = granted, entries = entries) }
    }

    private fun loadThumbnail(entry: ScreenshotEntry) {
        if (!requested.add(entry.id)) return
        viewModelScope.launch {
            val image = runCatching { interactor.thumbnail(entry).decodeToImageBitmap() }.getOrNull() ?: return@launch
            _state.update { it.copy(thumbnails = it.thumbnails + (entry.id to image)) }
        }
    }

    private fun trash(entry: ScreenshotEntry) = action {
        interactor.moveToTrash(entry)
        _state.update { state ->
            state.copy(entries = state.entries - entry, thumbnails = state.thumbnails - entry.id, notice = EditorNotice.Trashed)
        }
    }

    private fun action(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: ScreenshotException) {
            _state.update { it.copy(notice = EditorNotice.Failed(e.error)) }
        }
    }
}
