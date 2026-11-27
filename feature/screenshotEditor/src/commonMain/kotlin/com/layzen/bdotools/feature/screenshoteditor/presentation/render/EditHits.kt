package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.isDrawn
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** Наконечник стрелки длиннее её толщины — рамка выбора должна его вмещать. */
private const val ARROW_HEAD_REACH = 4.5f

/**
 * Прямоугольник, который занимает правка на картинке, в пикселях картинки: по нему рисуется рамка выбора.
 * Размер надписи меряет [measurer]; у обрезки, ретуши и поворота рамки нет.
 */
internal fun Edit.bounds(measurer: TextMeasurer): ImageRect? = when (this) {
    is Edit.Pixelate -> rect
    is Edit.Frame -> rect.grown(width / 2)
    is Edit.Arrow -> around(listOf(from, to), width * ARROW_HEAD_REACH)
    is Edit.Stroke -> around(points, width / 2)
    is Edit.Label -> labelRect(this, measurer)
    is Edit.Crop, is Edit.Adjust, is Edit.Orient -> null
}

/**
 * Номер верхней правки из [edits] под точкой [at] с запасом [tolerance] пикселей картинки; `null` — мимо всех.
 * Рамку берут за край, стрелку и линию — за линию, а скрытую область и надпись — где угодно внутри.
 */
internal fun hitIndex(edits: List<Edit>, at: ImagePoint, tolerance: Float, measurer: TextMeasurer): Int? =
    edits.indices.reversed().firstOrNull { index -> edits[index].isDrawn && edits[index].isHit(at, tolerance, measurer) }

private fun Edit.isHit(at: ImagePoint, tolerance: Float, measurer: TextMeasurer): Boolean = when (this) {
    is Edit.Pixelate -> at in rect.grown(tolerance)
    is Edit.Label -> at in labelRect(this, measurer).grown(tolerance)
    is Edit.Frame -> at in rect.grown(width / 2 + tolerance) && at !in rect.grown(-(width / 2 + tolerance))
    is Edit.Arrow -> distanceToLine(at, from, to) <= width / 2 + tolerance
    is Edit.Stroke -> points.zipWithNext().any { (a, b) -> distanceToLine(at, a, b) <= width / 2 + tolerance } ||
        points.singleOrNull()?.let { hypot(it.x - at.x, it.y - at.y) <= width / 2 + tolerance } == true
    is Edit.Crop, is Edit.Adjust, is Edit.Orient -> false
}

/** Надпись, измеренная в пикселях картинки: с плотностью 1 размер шрифта в sp и есть пиксели. */
private fun labelRect(label: Edit.Label, measurer: TextMeasurer): ImageRect {
    val size = measurer.measure(
        label.text,
        TextStyle(fontSize = label.size.sp, fontWeight = FontWeight.Bold),
        density = Density(1f),
    ).size
    return ImageRect(label.at.x, label.at.y, label.at.x + size.width, label.at.y + size.height)
}

private fun ImageRect.grown(by: Float) = ImageRect(left - by, top - by, right + by, bottom + by)

private fun around(points: List<ImagePoint>, margin: Float) = ImageRect(
    points.minOf { it.x } - margin,
    points.minOf { it.y } - margin,
    points.maxOf { it.x } + margin,
    points.maxOf { it.y } + margin,
)

/** Расстояние от точки [p] до отрезка [a]–[b]. */
private fun distanceToLine(p: ImagePoint, a: ImagePoint, b: ImagePoint): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val length = dx * dx + dy * dy
    val t = if (length == 0f) 0f else (((p.x - a.x) * dx + (p.y - a.y) * dy) / length).let { max(0f, min(1f, it)) }
    return hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy))
}
