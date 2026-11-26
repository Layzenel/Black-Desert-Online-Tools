package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Вариант из группы, как в настройках игры («Всегда / Никогда / Только в бою»): квадратик-индикатор и подпись.
 * Как вкладки в настройках игры: невыбранный — серая подложка, тёмный индикатор и приглушённый текст; под курсором —
 * светло-коричневая подложка и светлый текст; выбранный — коричневая подложка, светлая отметка в индикаторе.
 * Тёмная рамка снаружи и светлая кромка внутри, углы скруглены [AppShapes.Control].
 * [icon] — картинка (белый силуэт) вместо подписи, цвета подписи; [text] тогда — её описание для экранного диктора.
 * [content] — своё содержимое вместо подписи (текст с картинками клавиш), получает цвет подписи.
 */
@Composable
fun OptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter? = null,
    content: (@Composable (color: Color) -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val highlighted = hovered && enabled && !selected
    Row(
        modifier
            .heightIn(min = dimens.buttonHeight)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, null, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .border(dimens.borderWidth, colors.windowOutline, AppShapes.Control)
            .padding(dimens.borderWidth)
            .background(
                when {
                    selected -> colors.backgroundSelected
                    highlighted -> colors.optionHover
                    else -> colors.option
                },
                AppShapes.ControlInner,
            )
            .border(
                dimens.borderWidth,
                when {
                    selected -> colors.borderSelected
                    highlighted -> colors.optionHoverBorder
                    else -> colors.optionBorder
                },
                AppShapes.ControlInner,
            )
            .padding(horizontal = dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
    ) {
        Box(
            Modifier.size(dimens.checkboxSize)
                .background(colors.indicator)
                .border(dimens.borderWidth, if (selected) colors.indicatorBorderSelected else colors.indicatorBorder),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(Modifier.size(dimens.checkboxMarkSize).background(Brush.verticalGradient(listOf(colors.indicatorMark, colors.indicatorMarkShade))))
            }
        }
        val contentColor = if (selected || highlighted) colors.textSelected else colors.textDisabled
        if (content != null) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { content(contentColor) }
        } else if (icon != null) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                // высота — как у иконки, ширина — по пропорциям картинки: широкая клавиша (Shift, ряд стрелок) не сжимается в квадрат
                val intrinsic = icon.intrinsicSize
                val aspect = if (intrinsic.isSpecified && intrinsic.height > 0f) intrinsic.width / intrinsic.height else 1f
                Image(icon, contentDescription = text, modifier = Modifier.height(dimens.iconSize).aspectRatio(aspect), colorFilter = ColorFilter.tint(contentColor))
            }
        } else {
            Text(
                text,
                style = AppTheme.typography.bodyBold,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
        }
    }
}
