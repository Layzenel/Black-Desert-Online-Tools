package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.applyPlacement
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.EditorState
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import kotlin.math.min

private const val SCROLL_ZOOM_STEP = 1.1f

/** Область с изображением и маской. Вокруг сетки — поле в полячейки, чтобы было видно, что выходит за край. */
@Composable
internal fun MaskViewport(state: EditorState, onEvent: (PortraitsEvent) -> Unit) {
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
                .pointerInput(grid) {
                    detectTapGestures { tap ->
                        val cell = grid.cellAt(tap.x / currentK - margin, tap.y / currentK - margin)
                        if (cell != null) currentOnEvent(PortraitsEvent.ToggleMaskCell(cell))
                    }
                }
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
            MaskOverlay(grid, state.disabledCells, margin, k, Modifier.matchParentSize())
        }
    }
}
