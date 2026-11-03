package com.layzen.bdotools.feature.portraits.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.layzen.bdotools.feature.portraits.domain.PortraitGrid
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_hint
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_reset
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_rotation
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_title_all
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_title_one
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_zoom
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.min
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private const val SCROLL_ZOOM_STEP = 1.1f
private const val MASK_SCRIM_ALPHA = 0.7f
private val ZOOM_PERCENT_RANGE = 10..400
private val ROTATION_RANGE = -180..180

/**
 * Подгонка изображения под маску. Маска повторяет сетку окна выбора персонажа: ячейки прозрачные,
 * отступы и поля вокруг затемнены. Изображение двигается мышью, масштаб — колесом и слайдером.
 */
@Composable
internal fun MaskEditor(state: EditorState, onEvent: (PortraitsEvent) -> Unit) {
    val title = when (state.target) {
        ReplaceTarget.All -> Res.string.portraits_editor_title_all
        is ReplaceTarget.One -> Res.string.portraits_editor_title_one
    }
    ModalWindow(
        title = stringResource(title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseEditor) },
        modifier = Modifier.widthIn(max = PortraitsDimens.editorMaxWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_editor_reset), onClick = { onEvent(PortraitsEvent.ResetPlacement) })
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseEditor) })
            ActionButton(stringResource(Res.string.portraits_editor_apply), onClick = { onEvent(PortraitsEvent.Apply) })
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            DescriptionText(stringResource(Res.string.portraits_editor_hint))
            MaskViewport(state, onEvent)
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXl)) {
                val zoomPercent = (state.placement.scale / state.initialPlacement.scale * 100).roundToInt()
                Column(Modifier.weight(1f)) {
                    SectionHeader(stringResource(Res.string.portraits_editor_zoom))
                    ValueSlider(
                        value = zoomPercent.coerceIn(ZOOM_PERCENT_RANGE),
                        onValueChange = { onEvent(PortraitsEvent.ZoomImage(state.initialPlacement.scale * it / 100f)) },
                        range = ZOOM_PERCENT_RANGE,
                    )
                }
                Column(Modifier.weight(1f)) {
                    SectionHeader(stringResource(Res.string.portraits_editor_rotation))
                    ValueSlider(
                        value = state.placement.rotationDegrees.roundToInt(),
                        onValueChange = { onEvent(PortraitsEvent.RotateImage(it.toFloat())) },
                        range = ROTATION_RANGE,
                    )
                }
            }
        }
    }
}

/** Область с изображением и маской. Вокруг сетки — поле в полячейки, чтобы было видно, что выходит за край. */
@Composable
private fun MaskViewport(state: EditorState, onEvent: (PortraitsEvent) -> Unit) {
    val grid = state.grid
    val margin = grid.cellWidth / 2f
    val sheetWidth = grid.width + margin * 2
    val sheetHeight = grid.height + margin * 2
    val density = LocalDensity.current

    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { PortraitsDimens.editorViewportMaxHeight.toPx() }
        // пикселей экрана на пиксель маски
        val k = min(maxWidthPx / sheetWidth, maxHeightPx / sheetHeight)
        val viewportSize = with(density) { DpSize((sheetWidth * k).toDp(), (sheetHeight * k).toDp()) }
        val currentK by rememberUpdatedState(k)
        val currentPlacement by rememberUpdatedState(state.placement)
        val currentOnEvent by rememberUpdatedState(onEvent)

        Box(
            Modifier
                .size(viewportSize)
                .clipToBounds()
                .pointerHoverIcon(PointerIcon.Hand)
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        currentOnEvent(PortraitsEvent.MoveImage(drag.x / currentK, drag.y / currentK))
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type != PointerEventType.Scroll) continue
                            val delta = event.changes.first().scrollDelta.y
                            if (delta == 0f) continue
                            val factor = if (delta > 0) 1 / SCROLL_ZOOM_STEP else SCROLL_ZOOM_STEP
                            currentOnEvent(PortraitsEvent.ZoomImage(currentPlacement.scale * factor))
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
        ) {
            val image = state.image
            val placement = state.placement
            Canvas(Modifier.matchParentSize()) {
                drawRect(Color.Black)
                scale(k, k, pivot = Offset.Zero) {
                    translate(margin, margin) {
                        drawIntoCanvas { canvas ->
                            canvas.save()
                            canvas.applyPlacement(placement, image)
                            canvas.drawImage(image, Offset.Zero, Paint().apply { filterQuality = FilterQuality.Medium })
                            canvas.restore()
                        }
                    }
                }
            }
            MaskOverlay(grid, margin, k, Modifier.matchParentSize())
        }
    }
}

@Composable
private fun MaskOverlay(grid: PortraitGrid, margin: Float, k: Float, modifier: Modifier) {
    val scrim = AppTheme.colors.backgroundDeep.copy(alpha = MASK_SCRIM_ALPHA)
    val border = AppTheme.colors.accentGoldPale
    val borderWidth = with(LocalDensity.current) { PortraitsDimens.maskBorderWidth.toPx() }
    Canvas(modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        drawRect(scrim)
        (0 until grid.count).map(grid::cell).forEach { cell ->
            val topLeft = Offset((cell.x + margin) * k, (cell.y + margin) * k)
            val size = Size(cell.width * k, cell.height * k)
            drawRect(Color.Transparent, topLeft, size, blendMode = BlendMode.Clear)
            drawRect(border, topLeft, size, style = Stroke(borderWidth))
        }
    }
}
