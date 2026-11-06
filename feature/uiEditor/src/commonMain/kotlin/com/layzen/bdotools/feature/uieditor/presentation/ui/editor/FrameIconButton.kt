package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Значок-действие на рамке элемента холста: белый силуэт [icon] в цвет текста размером [iconSize], подпись
 * [description] — в подсказке при наведении. Нажатие поглощается, чтобы не схватить элемент под значком.
 */
@Composable
internal fun FrameIconButton(icon: Painter, description: String, iconSize: Dp, onClick: () -> Unit) {
    HoverTooltip(description) {
        Image(
            icon,
            contentDescription = description,
            modifier = Modifier.size(iconSize).consumingTap(onClick),
            colorFilter = ColorFilter.tint(AppTheme.colors.textPrimary),
        )
    }
}
