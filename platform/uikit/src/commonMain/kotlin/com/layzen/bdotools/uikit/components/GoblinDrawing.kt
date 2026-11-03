package com.layzen.bdotools.uikit.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

private const val TORSO_WIDTH = 15f
private const val LEG_WIDTH = 7f
private const val ARM_WIDTH = 5.5f
private const val HEAD_RADIUS = 11f
private const val BELLY_RADIUS = 11f

/**
 * Рисует гоблина одним цветом, вписав квадрат [GOBLIN_BOX] в центр области.
 * [grow] расширяет каждую часть на столько пикселей: так рисуется обводка,
 * поверх которой потом ложится силуэт с `grow = 0`.
 */
internal fun DrawScope.drawGoblin(pose: GoblinPose, color: Color, grow: Float) {
    val scale = size.minDimension / GOBLIN_BOX
    val origin = Offset((size.width - GOBLIN_BOX * scale) / 2, (size.height - GOBLIN_BOX * scale) / 2)
    fun at(point: Offset) = origin + point * scale
    fun limb(points: List<Offset>, width: Float) = points.zipWithNext { from, to ->
        drawLine(color, at(from), at(to), strokeWidth = width * scale + 2 * grow, cap = StrokeCap.Round)
    }
    fun triangle(points: List<Offset>) {
        val path = Path().apply {
            moveTo(at(points[0]).x, at(points[0]).y)
            points.drop(1).forEach { lineTo(at(it).x, at(it).y) }
            close()
        }
        drawPath(path, color)
        if (grow > 0f) drawPath(path, color, style = Stroke(2 * grow, join = StrokeJoin.Round))
    }

    limb(pose.legs[0], LEG_WIDTH)
    limb(pose.arms[0], ARM_WIDTH)
    limb(listOf(pose.hip, pose.shoulder), TORSO_WIDTH)
    drawCircle(color, BELLY_RADIUS * scale + grow, at(pose.belly))
    drawCircle(color, HEAD_RADIUS * scale + grow, at(pose.head))
    triangle(pose.ear)
    triangle(pose.nose)
    limb(pose.legs[1], LEG_WIDTH)
    limb(pose.arms[1], ARM_WIDTH)
}
