package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpSize
import com.layzen.bdotools.composable.canvas.rememberCanvasViewport
import com.layzen.bdotools.composable.overlay.GameOverlayWindow
import com.layzen.bdotools.composable.overlay.OverlayAnchor
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import com.layzen.bdotools.feature.screenshoteditor.presentation.ScreenshotEditorViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_cancel
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_quick_crop_close
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_quick_crop_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_quick_crop_title
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Окно быстрой обрезки запоминает место под этим именем. */
private const val QUICK_CROP_WINDOW = "screenshot-quick-crop"

/**
 * Быстрая обрезка поверх игры: скриншот из уведомления в окне по центру экрана. Обвели область и нажали галочку —
 * вырезанная часть в полном размере уходит в буфер обмена, и окно закрывается. Окно без фокуса, поэтому Esc и Enter
 * здесь не работают — только кнопки.
 */
@Composable
internal fun QuickCropWindow(quick: ScreenshotEditorViewModel) {
    val state by quick.state.collectAsState()
    val image = state.image ?: return
    // обрезке нужен инструмент «Обрезка»: в редакторе его по умолчанию нет
    LaunchedEffect(image) { quick.onEvent(ScreenshotEditorEvent.SelectTool(EditorTool.Crop)) }
    // скопировали или не вышло — окно больше не нужно
    LaunchedEffect(state.notice) {
        if (state.notice == null) return@LaunchedEffect
        quick.onEvent(ScreenshotEditorEvent.DismissNotice)
        quick.onEvent(ScreenshotEditorEvent.Close)
    }
    val width = ScreenshotEditorDimens.quickCropWidth
    val height = ScreenshotEditorDimens.quickCropHeight
    GameOverlayWindow(QUICK_CROP_WINDOW, OverlayAnchor.Center, DpSize(width, height)) {
        // шрифты есть только внутри окна: снаружи окон поверх игры их ещё не подставили
        val measurer = rememberTextMeasurer()
        val crop = state.history.crop
        // галочка кладёт обрезку в историю: значит, пора копировать
        LaunchedEffect(crop) {
            if (crop == null) return@LaunchedEffect
            quick.onEvent(ScreenshotEditorEvent.Scale(ExportScale.Full))
            quick.onEvent(ScreenshotEditorEvent.Copy(measurer))
        }
        TooltipPanel {
            Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(Res.string.screenshots_quick_crop_title), style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
                        DescriptionText(stringResource(Res.string.screenshots_quick_crop_hint))
                    }
                    ToolbarButton(
                        painterResource(Res.drawable.menu_cancel),
                        stringResource(Res.string.screenshots_quick_crop_close),
                        onClick = { quick.onEvent(ScreenshotEditorEvent.Close) },
                    )
                }
                Box(Modifier.size(width, height)) {
                    EditorCanvas(image, state, quick::onEvent, measurer, rememberCanvasViewport(image), tools = false)
                }
            }
        }
    }
}
