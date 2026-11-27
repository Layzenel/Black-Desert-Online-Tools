package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.feature.screenshoteditor.presentation.ScreenshotEditorViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_open
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_save
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_busy
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copy_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_save
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.BusyOverlay
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.Toast
import com.layzen.bdotools.uikit.components.ToolPage
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Вкладка редактора скриншотов. */
@Composable
fun ScreenshotEditorRoute(viewModel: ScreenshotEditorViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenshotEditorScreen(state, viewModel::onEvent)
}

/** Редактор: пока скриншота нет — вступление, потом меню слева, инструменты сверху и скриншот на всё место. */
@Composable
fun ScreenshotEditorScreen(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    val image = state.image
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (image == null) {
            EditorIntro(onEvent)
        } else {
            val measurer = rememberTextMeasurer()
            ToolPage(
                modifier = Modifier.fillMaxSize(),
                sidebar = {
                    ToolSettings(state, onEvent)
                    SectionDivider()
                    ExportSettings(state, onEvent)
                },
                sidebarFooter = { FileActions(onEvent, onCopy = { onEvent(ScreenshotEditorEvent.Copy(measurer)) }, onSave = { onEvent(ScreenshotEditorEvent.Save(measurer)) }) },
            ) {
                Column(Modifier.fillMaxSize()) {
                    EditorToolbar(state, onEvent)
                    EditorCanvas(image, state, onEvent, measurer, Modifier.weight(1f).padding(AppTheme.dimens.spaceL))
                }
            }
        }
        if (state.busy) BusyOverlay(stringResource(Res.string.screenshots_busy))
        val notice = state.notice
        Toast(
            text = notice?.let { noticeText(it) },
            onDismiss = { onEvent(ScreenshotEditorEvent.DismissNotice) },
            isError = notice is EditorNotice.Failed,
        )
    }
}

/** Открыть другой скриншот, скопировать и последним — главное действие «Сохранить копию». */
@Composable
private fun FileActions(onEvent: (ScreenshotEditorEvent) -> Unit, onCopy: () -> Unit, onSave: () -> Unit) {
    ActionButton(
        ActionButtonState.Neutral(
            stringResource(Res.string.screenshots_open),
            icon = painterResource(Res.drawable.menu_open),
            onClick = { onEvent(ScreenshotEditorEvent.Open) },
            tooltip = stringResource(Res.string.screenshots_open_tooltip),
        ),
        Modifier.fillMaxWidth(),
    )
    ActionButton(
        ActionButtonState.Neutral(
            stringResource(Res.string.screenshots_copy),
            icon = painterResource(Res.drawable.menu_copy),
            onClick = onCopy,
            tooltip = stringResource(Res.string.screenshots_copy_tooltip),
        ),
        Modifier.fillMaxWidth(),
    )
    ActionButton(
        ActionButtonState.Positive(
            stringResource(Res.string.screenshots_save),
            icon = painterResource(Res.drawable.menu_save),
            onClick = onSave,
            tooltip = stringResource(saveTooltip),
        ),
        Modifier.fillMaxWidth(),
    )
}

/** Строки вкладки заранее, чтобы при первом открытии кнопки не мелькали пустыми. */
suspend fun preloadScreenshotEditorStrings() {
    getString(Res.string.screenshots_open)
}
