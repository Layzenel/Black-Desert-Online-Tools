package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

// Сторона «пикселя» пикселизации — сотая длинной стороны картинки: ник в 4K и в Full HD скрывается одинаково
private const val PIXEL_BLOCKS = 100
private const val MIN_BLOCK = 6
private const val ARROW_HEAD_PER_WIDTH = 4.5f
private const val ARROW_HEAD_ANGLE = 0.45f
private const val LABEL_SHADOW_PER_SIZE = 0.06f

/**
 * Рисует скриншот [source] с правками в координатах картинки: один и тот же код и для экрана (с масштабом
 * поверх), и для сохранения — поэтому сохранённое совпадает с увиденным. Надписи меряет [measurer].
 * Уменьшенные копии пикселизованных областей кэширует: экран перерисовывается часто.
 */
internal class EditPainter(private val source: ImageBitmap, private val measurer: TextMeasurer) {
    private val block = max(MIN_BLOCK, max(source.width, source.height) / PIXEL_BLOCKS)
    private val tiles = mutableMapOf<ImageRect, ImageBitmap>()

    /** Скриншот и [edits] по порядку. */
    fun DrawScope.paint(edits: List<Edit>) {
        drawImage(source)
        edits.forEach { paintEdit(it) }
    }

    /** Одна правка поверх уже нарисованного. Обрезку не рисует: её показывает экран и применяет сохранение. */
    fun DrawScope.paintEdit(edit: Edit) {
        when (edit) {
            is Edit.Crop -> Unit
            is Edit.Pixelate -> pixelate(edit.rect)
            is Edit.Arrow -> arrow(edit)
            is Edit.Frame -> drawRect(
                Color(edit.color),
                Offset(edit.rect.left, edit.rect.top),
                Size(edit.rect.width, edit.rect.height),
                style = Stroke(edit.width, join = StrokeJoin.Miter),
            )
            is Edit.Stroke -> stroke(edit)
            is Edit.Label -> label(edit)
        }
    }

    private fun DrawScope.pixelate(rect: ImageRect) {
        if (rect.isEmpty) return
        val tile = tiles.getOrPut(rect) { shrink(rect) }
        drawImage(
            tile,
            dstOffset = IntOffset(rect.left.roundToInt(), rect.top.roundToInt()),
            dstSize = IntSize(rect.width.roundToInt(), rect.height.roundToInt()),
            filterQuality = FilterQuality.None,
        )
    }

    /** Область [rect], уменьшенная до «пикселя» на блок: растянутая без сглаживания, она и есть пикселизация. */
    private fun shrink(rect: ImageRect): ImageBitmap {
        val size = IntSize(max(1, (rect.width / block).roundToInt()), max(1, (rect.height / block).roundToInt()))
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
        val from = edit.from.offset
        val to = edit.to.offset
        drawLine(color, from, to, edit.width, StrokeCap.Round)
        val angle = atan2(to.y - from.y, to.x - from.x)
        val head = edit.width * ARROW_HEAD_PER_WIDTH
        listOf(angle + PI.toFloat() - ARROW_HEAD_ANGLE, angle + PI.toFloat() + ARROW_HEAD_ANGLE).forEach { side ->
            drawLine(color, to, to + Offset(cos(side) * head, sin(side) * head), edit.width, StrokeCap.Round)
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
        val style = TextStyle(
            color = Color(edit.color),
            fontSize = (edit.size / density / fontScale).toSp(),
            fontWeight = FontWeight.Bold,
            shadow = Shadow(Color.Black, Offset(edit.size, edit.size) * LABEL_SHADOW_PER_SIZE, edit.size * LABEL_SHADOW_PER_SIZE),
        )
        drawText(measurer, edit.text, edit.at.offset, style)
    }

    private val ImagePoint.offset get() = Offset(x, y)
}
