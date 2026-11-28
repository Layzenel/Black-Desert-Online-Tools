package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Resize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Snap
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.color
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.handlePoints
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.isDrawn
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedBy
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.hitIndex
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/** Во сколько раз Shift+колесо увеличивает выбранную пометку за щелчок. */
private const val WHEEL_SCALE_STEP = 1.1f

/**
 * Где на холсте мышь и что под ней: перевод точек окна [viewport] в пиксели картинки через [fit], поиск правки
 * или ручки выбранной правки и привязка сдвигаемой пометки. Размер надписей меряет [measurer].
 */
internal class CanvasPointer(
    private val state: ScreenshotEditorState,
    private val fit: ImageFit,
    private val viewport: CanvasViewport,
    private val measurer: TextMeasurer,
) {
    private val imagePixelsPerPoint: Float get() = 1 / (fit.scale * viewport.zoom)

    /** Точка окна холста [position] в пикселях картинки. */
    fun toImage(position: Offset): ImagePoint = fit.toImage(viewport.toContent(position))

    /** Номер верхней правки под точкой окна [position] с запасом [tolerance] точек окна; `null` — мимо всех. */
    fun hit(position: Offset, tolerance: Float): Int? = hitIndex(state.history.edits, toImage(position), tolerance * imagePixelsPerPoint, measurer)

    /**
     * Нажатие левой кнопкой в [position]: над ручкой выбранной правки (ближе [reach] точек окна), над правкой
     * (с запасом [tolerance] точек окна) или мимо.
     */
    fun press(position: Offset, tolerance: Float, reach: Float): ScreenshotEditorEvent.Press {
        val resize = resizeAt(position, reach)
        return ScreenshotEditorEvent.Press(toImage(position), if (resize != null) null else hit(position, tolerance), resize)
    }

    /**
     * Колесо над выбранной пометкой в [position]: вверх ([delta] < 0) — толще (у скрытой области — сильнее), вниз —
     * тоньше; с [shift] — больше или меньше целиком. `null` — колесо не над выбранной пометкой и масштабирует холст.
     */
    fun wheel(position: Offset, delta: Float, shift: Boolean): ScreenshotEditorEvent? {
        val selection = state.shownSelection ?: return null
        val bounds = selection.bounds(measurer) ?: return null
        if (delta == 0f || toImage(position) !in bounds) return null
        val step = if (delta < 0) 1 else -1
        return when {
            shift -> ScreenshotEditorEvent.ScaleSelected(if (step > 0) WHEEL_SCALE_STEP else 1 / WHEEL_SCALE_STEP, bounds.center)
            selection is Edit.Pixelate -> ScreenshotEditorEvent.HideStrength(selection.strength + step)
            selection.color != null -> ScreenshotEditorEvent.Thickness(state.thickness + step)
            else -> null
        }
    }

    /**
     * Куда сдвинуть правку номер [index], которую взяли в [start] и тянут к [to]: её края и середина прилипают к краям
     * и серединам других пометок и видимой части картинки ближе [reach] точек окна. Вторым — где провести направляющие.
     */
    fun snapped(index: Int, start: ImagePoint, to: ImagePoint, reach: Float): Pair<ImagePoint, Snap> {
        val bounds = state.history.edits.getOrNull(index)?.bounds(measurer) ?: return to to Snap()
        val targets = state.history.edits
            .filterIndexed { i, edit -> i != index && edit.isDrawn }
            .mapNotNull { it.bounds(measurer) } + state.bounds
        val snap = Snap.of(bounds.movedBy(to.x - start.x, to.y - start.y), targets, reach * imagePixelsPerPoint)
        return ImagePoint(to.x + snap.dx, to.y + snap.dy) to snap
    }

    private fun resizeAt(position: Offset, reach: Float): Resize? {
        val selection = state.shownSelection ?: return null
        val bounds = selection.bounds(measurer) ?: return null
        val handle = selection.handlePoints(bounds).entries.firstOrNull { (_, point) ->
            (viewport.toWindow(fit.toScreen(point)) - position).getDistance() <= reach
        } ?: return null
        return Resize(handle.key, bounds)
    }
}
