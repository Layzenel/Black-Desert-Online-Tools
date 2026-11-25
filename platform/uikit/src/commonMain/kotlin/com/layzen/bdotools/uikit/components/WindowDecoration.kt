package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import com.layzen.bdotools.uikit.theme.AppTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

/**
 * Украшение темы окон над шапкой, как в игре: картинки над левым и правым краем окна стоят на его верхней кромке и
 * рисуются за пределами элемента, места в раскладке не занимают. Родитель не должен обрезать то, что выше окна.
 * В классической теме украшения нет.
 */
@Composable
fun Modifier.windowDecoration(): Modifier {
    val style = AppTheme.window
    val left = style.decorationLeft?.let { painterResource(it) }
    val right = style.decorationRight?.let { painterResource(it) }
    if (left == null && right == null) return this
    return drawBehind {
        left?.let { drawAbove(it, alignRight = false) }
        right?.let { drawAbove(it, alignRight = true) }
    }
}

/** Рисует [painter] в натуральную величину (1 px картинки = 1 dp) над верхней кромкой, у левого или правого края. */
private fun DrawScope.drawAbove(painter: Painter, alignRight: Boolean) {
    val size = Size(painter.intrinsicSize.width * density, painter.intrinsicSize.height * density)
    translate(left = if (alignRight) this.size.width - size.width else 0f, top = -size.height) {
        with(painter) { draw(size) }
    }
}

/** Высота самого высокого украшения темы — столько места нужно над окном, чтобы оно не обрезалось. */
@Composable
fun windowDecorationHeight(): Dp {
    val style = AppTheme.window
    val heights = listOfNotNull(style.decorationLeft, style.decorationRight).map { painterResource(it).intrinsicSize.height }
    return (heights.maxOrNull() ?: 0f).dp
}
