package com.layzen.bdotools.feature.comboassist.presentation.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Стрелка между шагами комбо: вправо или, при [vertical], вниз; цвета [color]. */
@Composable
internal fun StepArrow(modifier: Modifier = Modifier, vertical: Boolean = false, color: Color = AppTheme.colors.textBeige) {
    val box = if (vertical) Modifier.size(ComboDimens.arrowHeight, ComboDimens.arrowLength) else Modifier.size(ComboDimens.arrowLength, ComboDimens.arrowHeight)
    Canvas(modifier.then(box)) {
        val length = if (vertical) size.height else size.width
        val thickness = if (vertical) size.width else size.height
        rotate(if (vertical) RIGHT_ANGLE else 0f, pivot = Offset(size.width / 2, size.height / 2)) {
            val left = (size.width - length) / 2
            val top = (size.height - thickness) / 2
            val y = top + thickness / 2
            val head = thickness / 2
            drawLine(color, Offset(left, y), Offset(left + length - head, y), strokeWidth = thickness / STROKE_SHARE)
            drawPath(Path().apply { moveTo(left + length, y); lineTo(left + length - head, top); lineTo(left + length - head, top + thickness); close() }, color)
        }
    }
}

/** Знак связки — два звена цепи: шаги жмутся подряд без паузы. */
@Composable
internal fun ChainLink(modifier: Modifier = Modifier, vertical: Boolean = false) {
    val color = AppTheme.colors.accentGold
    val box = if (vertical) Modifier.size(ComboDimens.chainHeight, ComboDimens.arrowLength) else Modifier.size(ComboDimens.arrowLength, ComboDimens.chainHeight)
    Canvas(modifier.then(box)) {
        rotate(if (vertical) RIGHT_ANGLE else 0f, pivot = Offset(size.width / 2, size.height / 2)) {
            val length = if (vertical) size.height else size.width
            val thickness = if (vertical) size.width else size.height
            val left = (size.width - length) / 2
            val top = (size.height - thickness) / 2
            val stroke = ComboDimens.frame.toPx()
            // звено — скруглённая рамка на 60 % длины; второе сдвинуто вправо и заходит в первое
            val link = Size(length * LINK_SHARE, thickness - stroke)
            val corner = CornerRadius(link.height / 2)
            drawRoundRect(color, Offset(left, top + stroke / 2), link, corner, style = Stroke(stroke))
            drawRoundRect(color, Offset(left + length - link.width, top + stroke / 2), link, corner, style = Stroke(stroke))
        }
    }
}
/** Скобка развилки: [start] — открывающая. Вдоль развилки — по её высоте или, при [vertical], по ширине. */
@Composable
internal fun ForkBracket(start: Boolean, modifier: Modifier = Modifier, vertical: Boolean = false) {
    val color = AppTheme.colors.textMuted
    val box = if (vertical) Modifier.height(ComboDimens.bracketWidth).fillMaxWidth() else Modifier.width(ComboDimens.bracketWidth).fillMaxHeight()
    Canvas(modifier.then(box)) {
        val stroke = ComboDimens.frame.toPx()
        if (vertical) {
            val y = if (start) 0f else size.height
            drawLine(color, Offset(0f, y), Offset(size.width, y), stroke)
            drawLine(color, Offset(stroke / 2, 0f), Offset(stroke / 2, size.height), stroke)
            drawLine(color, Offset(size.width - stroke / 2, 0f), Offset(size.width - stroke / 2, size.height), stroke)
        } else {
            val x = if (start) 0f else size.width
            drawLine(color, Offset(x, 0f), Offset(x, size.height), stroke)
            drawLine(color, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
            drawLine(color, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
        }
    }
}

/** Значок «развилка»: одна линия расходится на две. */
@Composable
internal fun ForkIcon(modifier: Modifier = Modifier) {
    val color = AppTheme.colors.textMuted
    Canvas(modifier.size(ComboDimens.forkIcon)) {
        val stroke = Stroke(width = ComboDimens.frame.toPx())
        drawPath(
            Path().apply {
                moveTo(0f, size.height / 2); lineTo(size.width / 2, size.height / 2)
                lineTo(size.width, 0f)
                moveTo(size.width / 2, size.height / 2); lineTo(size.width, size.height)
            },
            color, style = stroke,
        )
    }
}

private const val RIGHT_ANGLE = 90f
private const val STROKE_SHARE = 5f
private const val LINK_SHARE = 0.6f

/** Значок «+», нарисованный линиями: символ шрифта стоит не по центру кнопки. */
@Composable
internal fun PlusIcon(modifier: Modifier = Modifier) {
    val color = AppTheme.colors.textMuted
    Canvas(modifier.size(ComboDimens.plusIcon)) {
        val stroke = ComboDimens.frame.toPx()
        drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), stroke)
        drawLine(color, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), stroke)
    }
}

/** Значок «×», нарисованный линиями: удалить. */
@Composable
internal fun CrossIcon(modifier: Modifier = Modifier) {
    val color = AppTheme.colors.textPrimary
    Canvas(modifier.size(ComboDimens.crossIcon)) {
        val stroke = ComboDimens.frame.toPx()
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), stroke)
    }
}
