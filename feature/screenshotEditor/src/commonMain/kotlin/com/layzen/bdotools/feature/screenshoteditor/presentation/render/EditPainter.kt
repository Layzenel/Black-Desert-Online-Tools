package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.colorMatrix
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ArrowStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.DEFAULT_HIDE_STRENGTH
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.FrameShape
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.HideMode
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.LabelStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.line
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.outline
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

// Сторона «пикселя» пикселизации — сотая длинной стороны картинки: ник в 4K и в Full HD скрывается одинаково
private const val PIXEL_BLOCKS = 100
private const val MIN_BLOCK = 6

// Размытие — та же уменьшенная копия, но растянутая со сглаживанием: блок вдвое крупнее, чтобы ник не читался
private const val BLUR_BLOCK_FACTOR = 2
private const val ARROW_HEAD_PER_WIDTH = 4.5f
private const val ARROW_HEAD_ANGLE = 0.45f
private const val DASH_PER_WIDTH = 3f
private const val GAP_PER_WIDTH = 2f
private const val MARKER_ALPHA = 0.35f
private const val LABEL_SHADOW_PER_SIZE = 0.06f
private const val LABEL_OUTLINE_PER_SIZE = 0.12f
private const val PLATE_ALPHA = 0.65f
private const val COUNTER_TEXT_PER_RADIUS = 1.1f
private const val COUNTER_RING_PER_RADIUS = 0.1f

// Номер на светлом кружке — тёмный, на тёмном — белый
private const val LIGHT_LUMINANCE = 0.6f

/**
 * Рисует скриншот [source] с правками в координатах картинки: один и тот же код и для экрана (с масштабом
 * поверх), и для сохранения — поэтому сохранённое совпадает с увиденным. Надписи меряет [measurer].
 * Уменьшенные копии скрытых областей кэширует: экран перерисовывается часто.
 */
internal class EditPainter(private val source: ImageBitmap, private val measurer: TextMeasurer) {
    private val block = max(MIN_BLOCK, max(source.width, source.height) / PIXEL_BLOCKS)
    private val tiles = mutableMapOf<Edit.Pixelate, ImageBitmap>()
    private var adjustments = Adjustments()
    private var filter: ColorFilter? = null

    /**
     * Скриншот с ретушью [adjustments] и [edits] по порядку. Виньетка ложится по краям [frame] — той части картинки,
     * что останется после обрезки.
     */
    fun DrawScope.paint(edits: List<Edit>, adjustments: Adjustments, frame: ImageRect) {
        if (adjustments != this@EditPainter.adjustments) {
            this@EditPainter.adjustments = adjustments
            filter = if (adjustments.changesColor) ColorFilter.colorMatrix(ColorMatrix(adjustments.colorMatrix())) else null
        }
        drawImage(source, colorFilter = filter)
        if (adjustments.vignette > 0) vignette(frame, adjustments.vignette)
        edits.forEach { paintEdit(it) }
    }

    /** Одна правка поверх уже нарисованного. Обрезку не рисует: её показывает экран и применяет сохранение. */
    fun DrawScope.paintEdit(edit: Edit) {
        when (edit) {
            is Edit.Crop, is Edit.Adjust, is Edit.Orient -> Unit
            is Edit.Pixelate -> hide(edit)
            is Edit.Arrow -> arrow(edit)
            is Edit.Frame -> frame(edit)
            is Edit.Stroke -> stroke(edit)
            is Edit.Label -> label(edit)
            is Edit.Counter -> counter(edit)
        }
    }

    private fun DrawScope.hide(edit: Edit.Pixelate) {
        val rect = edit.rect
        if (rect.isEmpty) return
        val tile = tiles.getOrPut(edit) { shrink(edit) }
        drawImage(
            tile,
            dstOffset = IntOffset(rect.left.roundToInt(), rect.top.roundToInt()),
            dstSize = IntSize(rect.width.roundToInt(), rect.height.roundToInt()),
            filterQuality = if (edit.mode == HideMode.Blur) FilterQuality.High else FilterQuality.None,
            colorFilter = filter,
        )
    }

    /**
     * Область, уменьшенная до «пикселя» на блок: растянутая без сглаживания, она и есть пикселизация, со сглаживанием —
     * размытие. Блок крупнее с силой скрытия.
     */
    private fun shrink(edit: Edit.Pixelate): ImageBitmap {
        val rect = edit.rect
        val factor = if (edit.mode == HideMode.Blur) BLUR_BLOCK_FACTOR else 1
        val side = max(1f, block.toFloat() * edit.strength / DEFAULT_HIDE_STRENGTH * factor)
        val size = IntSize(max(1, (rect.width / side).roundToInt()), max(1, (rect.height / side).roundToInt()))
        val tile = ImageBitmap(size.width, size.height)
        Canvas(tile).drawImageRect(
            source,
            srcOffset = IntOffset(rect.left.roundToInt(), rect.top.roundToInt()),
            srcSize = IntSize(rect.width.roundToInt(), rect.height.roundToInt()),
            dstSize = size,
            paint = Paint().apply { filterQuality = FilterQuality.Medium },
        )
        return tile
    }

    private fun DrawScope.arrow(edit: Edit.Arrow) {
        val color = Color(edit.color)
        val points = edit.line.map { it.offset }
        val dash = if (edit.style == ArrowStyle.Dashed) PathEffect.dashPathEffect(floatArrayOf(edit.width * DASH_PER_WIDTH, edit.width * GAP_PER_WIDTH)) else null
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color, style = Stroke(edit.width, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = dash))
        if (edit.style == ArrowStyle.Line) return
        arrowHead(color, points[points.size - 2], points.last(), edit.width)
        if (edit.style == ArrowStyle.Double) arrowHead(color, points[1], points.first(), edit.width)
    }

    /** Наконечник у [tip], смотрит по направлению от [before] к [tip]. */
    private fun DrawScope.arrowHead(color: Color, before: Offset, tip: Offset, width: Float) {
        val angle = atan2(tip.y - before.y, tip.x - before.x)
        val head = width * ARROW_HEAD_PER_WIDTH
        listOf(angle + PI.toFloat() - ARROW_HEAD_ANGLE, angle + PI.toFloat() + ARROW_HEAD_ANGLE).forEach { side ->
            drawLine(color, tip, tip + Offset(cos(side) * head, sin(side) * head), width, StrokeCap.Round)
        }
    }

    private fun DrawScope.frame(edit: Edit.Frame) {
        val color = Color(edit.color)
        val rect = edit.rect
        val topLeft = Offset(rect.left, rect.top)
        val size = Size(rect.width, rect.height)
        when (edit.shape) {
            FrameShape.Marker -> drawRect(color.copy(alpha = MARKER_ALPHA), topLeft, size)
            FrameShape.Rectangle -> drawRect(color, topLeft, size, style = Stroke(edit.width, join = StrokeJoin.Miter))
            FrameShape.Ellipse -> drawOval(color, topLeft, size, style = Stroke(edit.width))
            FrameShape.Rhombus, FrameShape.Triangle, FrameShape.Star -> {
                val outline = edit.outline
                val path = Path().apply {
                    moveTo(outline.first().x, outline.first().y)
                    outline.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(path, color, style = Stroke(edit.width, join = StrokeJoin.Round))
            }
        }
    }

    private fun DrawScope.stroke(edit: Edit.Stroke) {
        val first = edit.points.firstOrNull() ?: return
        val color = Color(edit.color)
        if (edit.points.size == 1) {
            drawCircle(color, edit.width / 2, first.offset)
            return
        }
        val path = Path().apply {
            moveTo(first.x, first.y)
            edit.points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color, style = Stroke(edit.width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    private fun DrawScope.label(edit: Edit.Label) {
        // размер задан в пикселях картинки, а шрифт меряется в sp: делим на плотность этого холста
        val base = TextStyle(color = Color(edit.color), fontSize = (edit.size / density / fontScale).toSp(), fontWeight = FontWeight.Bold)
        when (edit.style) {
            LabelStyle.Shadow -> drawText(
                measurer,
                edit.text,
                edit.at.offset,
                base.copy(shadow = Shadow(Color.Black, Offset(edit.size, edit.size) * LABEL_SHADOW_PER_SIZE, edit.size * LABEL_SHADOW_PER_SIZE)),
            )
            LabelStyle.Plate -> {
                val plate = labelPlate(edit, measurer)
                drawRect(Color.Black.copy(alpha = PLATE_ALPHA), Offset(plate.left, plate.top), Size(plate.width, plate.height))
                drawText(measurer, edit.text, edit.at.offset, base)
            }
            LabelStyle.Outline -> {
                val outline = Stroke(edit.size * LABEL_OUTLINE_PER_SIZE, join = StrokeJoin.Round)
                drawText(measurer, edit.text, edit.at.offset, base.copy(color = Color.Black, drawStyle = outline))
                drawText(measurer, edit.text, edit.at.offset, base)
            }
        }
    }

    private fun DrawScope.counter(edit: Edit.Counter) {
        val color = Color(edit.color)
        val text = if (color.luminance() > LIGHT_LUMINANCE) Color.Black else Color.White
        drawCircle(color, edit.radius, edit.center.offset)
        drawCircle(text, edit.radius, edit.center.offset, style = Stroke(edit.radius * COUNTER_RING_PER_RADIUS))
        val style = TextStyle(color = text, fontSize = (edit.radius * COUNTER_TEXT_PER_RADIUS / density / fontScale).toSp(), fontWeight = FontWeight.Bold)
        val layout = measurer.measure(edit.number.toString(), style)
        drawText(layout, topLeft = edit.center.offset - Offset(layout.size.width / 2f, layout.size.height / 2f))
    }

    private val ImagePoint.offset get() = Offset(x, y)
}
