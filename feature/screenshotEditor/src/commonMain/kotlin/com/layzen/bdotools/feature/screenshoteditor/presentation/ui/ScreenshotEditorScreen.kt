package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.feature.screenshoteditor.presentation.ScreenshotEditorViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryState
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_busy
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open
import com.layzen.bdotools.uikit.components.BusyOverlay
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.Toast
import com.layzen.bdotools.uikit.components.ToolPage
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Вкладка редактора скриншотов. */
@Composable
fun ScreenshotEditorRoute(
    viewModel: ScreenshotEditorViewModel = koinViewModel(),
    gallery: GalleryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val galleryState by gallery.state.collectAsStateWithLifecycle()
    ScreenshotEditorScreen(state, viewModel::onEvent, galleryState, gallery::onEvent)
}

/**
 * Редактор: пока скриншот не открыт — галерея папки `ScreenShot` (или вступление, если доступа к ней нет), потом
 * меню слева, инструменты сверху и скриншот на всё место.
 */
@Composable
fun ScreenshotEditorScreen(
    state: ScreenshotEditorState,
    onEvent: (ScreenshotEditorEvent) -> Unit,
    gallery: GalleryState,
    onGalleryEvent: (GalleryEvent) -> Unit,
) {
    val image = state.image
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (image == null) {
            GalleryScreen(gallery, onGalleryEvent, { onEvent(ScreenshotEditorEvent.OpenEntry(it)) }, { onEvent(ScreenshotEditorEvent.Open) })
        } else {
            val measurer = rememberTextMeasurer()
            ToolPage(
                modifier = Modifier.fillMaxSize(),
                sidebar = {
                    ToolSettings(state, onEvent)
                    SectionDivider()
                    AdjustSettings(state, onEvent)
                    SectionDivider()
                    ExportSettings(state, onEvent)
                },
                sidebarFooter = { FileActions(onEvent, measurer) },
            ) {
                Column(Modifier.fillMaxSize()) {
                    EditorToolbar(state, onEvent)
                    EditorCanvas(image, state, onEvent, measurer, Modifier.weight(1f).padding(AppTheme.dimens.spaceL))
                }
            }
        }
        if (state.busy) BusyOverlay(stringResource(Res.string.screenshots_busy))
        val notice = state.notice ?: gallery.notice
        Toast(
            text = notice?.let { noticeText(it) },
            onDismiss = {
                onEvent(ScreenshotEditorEvent.DismissNotice)
                onGalleryEvent(GalleryEvent.DismissNotice)
            },
            isError = notice is EditorNotice.Failed,
        )
    }
}

/** Строки вкладки заранее, чтобы при первом открытии кнопки не мелькали пустыми. */
suspend fun preloadScreenshotEditorStrings() {
    getString(Res.string.screenshots_open)
}
