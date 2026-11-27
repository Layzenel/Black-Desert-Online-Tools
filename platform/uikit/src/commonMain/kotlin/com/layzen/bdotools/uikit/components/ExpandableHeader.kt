package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.uikit.theme.AppTheme

private const val ARROW_INSET = 0.2f

/**
 * Заголовок свёрнутого раздела меню: треугольник и [SectionHeader]. Клик переключает [open]; пояснение [description]
 * видно, только пока раздел раскрыт. Содержимое раздела вызывающий показывает сам, когда [open].
 */
@Composable
fun ExpandableHeader(title: String, open: Boolean, onToggle: () -> Unit, description: String? = null) {
    Row(
        Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand).clickable(onClick = onToggle),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExpandArrow(open)
        SectionHeader(title, description = if (open) description else null)
    }
}

/** Треугольник раскрытия: вправо — свёрнуто, вниз — раскрыто. Нарисован: символов ▸ ▾ в шрифте сайта нет. */
@Composable
private fun ExpandArrow(open: Boolean) {
    val color = AppTheme.colors.textBeige
    Canvas(Modifier.size(AppTheme.dimens.expandArrowSize)) {
        val path = Path().apply {
            if (open) {
                moveTo(0f, size.height * ARROW_INSET)
                lineTo(size.width, size.height * ARROW_INSET)
                lineTo(size.width / 2, size.height * (1 - ARROW_INSET))
            } else {
                moveTo(size.width * ARROW_INSET, 0f)
                lineTo(size.width * (1 - ARROW_INSET), size.height / 2)
                lineTo(size.width * ARROW_INSET, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}
