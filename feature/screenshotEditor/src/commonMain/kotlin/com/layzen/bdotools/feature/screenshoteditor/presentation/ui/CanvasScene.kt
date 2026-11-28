package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Snap
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.EditPainter
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/**
 * Всё, что нужно, чтобы нарисовать холст редактора: скриншот [image] и его [state], рисовальщик правок [painter],
 * вписывание [fit] и масштаб [viewport]. Размер надписей для рамки выбора меряет [measurer]; [guides] — направляющие
 * привязки, пока пометку тянут.
 */
internal class CanvasScene(
    val image: ImageBitmap,
    val state: ScreenshotEditorState,
    val painter: EditPainter,
    val fit: ImageFit,
    val viewport: CanvasViewport,
    val measurer: TextMeasurer,
    val guides: Snap = Snap(),
) {
    /** Сколько точек экрана в пикселе картинки: рамки на экране — одной толщины при любом масштабе. */
    val pixelsPerImagePixel: Float get() = fit.scale * viewport.zoom
}
