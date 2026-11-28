package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_apply
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_cancel
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_crop_cancel
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_crop_confirm
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_delete
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Кнопки прямо на скриншоте: под обведённой обрезкой — зелёная галочка и красный крестик, над выбранной правкой —
 * крестик «Удалить». Пока правку или обрезку тянут, кнопок нет: они бы прыгали за мышью.
 */
@Composable
internal fun CanvasControls(
    state: ScreenshotEditorState,
    fit: ImageFit,
    viewport: CanvasViewport,
    measurer: TextMeasurer,
    onEvent: (ScreenshotEditorEvent) -> Unit,
) {
    if (state.dragOffset != null || state.resized != null || state.comparing) return
    val colors = AppTheme.colors
    state.pendingCrop?.let { crop ->
        val rect = screenRect(crop, fit, viewport)
        Row(
            Modifier.cornerAt(Offset(rect.right, rect.bottom), above = false),
            horizontalArrangement = Arrangement.spacedBy(ScreenshotEditorDimens.canvasButtonGap),
        ) {
            CanvasButton(painterResource(Res.drawable.menu_apply), colors.success, stringResource(Res.string.screenshots_crop_confirm)) {
                onEvent(ScreenshotEditorEvent.ConfirmCrop)
            }
            CanvasButton(painterResource(Res.drawable.menu_cancel), colors.danger, stringResource(Res.string.screenshots_crop_cancel)) {
                onEvent(ScreenshotEditorEvent.Cancel)
            }
        }
    }
    state.shownSelection?.bounds(measurer)?.let { selection ->
        val rect = screenRect(selection, fit, viewport)
        CanvasButton(
            painterResource(Res.drawable.menu_cancel),
            colors.danger,
            stringResource(Res.string.screenshots_delete),
            Modifier.cornerAt(Offset(rect.right, rect.top), above = true),
        ) { onEvent(ScreenshotEditorEvent.DeleteSelected) }
    }
}

/** Прямоугольник картинки [rect] в точках окна холста — с учётом вписывания и масштаба. */
private fun screenRect(rect: ImageRect, fit: ImageFit, viewport: CanvasViewport): Rect {
    val shown = fit.toScreen(rect)
    return Rect(viewport.toWindow(shown.topLeft), viewport.toWindow(shown.bottomRight))
}

/**
 * Ставит правый край содержимого на [corner].x, а его низ ([above]) или верх — на [corner].y с отступом, не выпуская
 * его за края холста: у края экрана кнопки встают внутрь рамки.
 */
private fun Modifier.cornerAt(corner: Offset, above: Boolean): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val gap = ScreenshotEditorDimens.canvasButtonGap.toPx()
    val x = (corner.x - placeable.width).coerceIn(0f, (constraints.maxWidth - placeable.width).toFloat().coerceAtLeast(0f))
    val y = (if (above) corner.y - placeable.height - gap else corner.y + gap)
        .coerceIn(0f, (constraints.maxHeight - placeable.height).toFloat().coerceAtLeast(0f))
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(x.roundToInt(), y.roundToInt()) }
}
