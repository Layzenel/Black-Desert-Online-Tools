package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
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
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Серая прямоугольная кнопка с лёгким градиентом, как «Применить», «Сброс», «Выход».
 * В игре нет «цветных» кнопок: главное действие выделяется только позицией (справа в футере).
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ActionButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(
            text = text,
            style = AppTheme.typography.button,
            color = if (enabled) AppTheme.colors.textButton else AppTheme.colors.textDisabled,
            maxLines = 1,
        )
    }
}

/** Кнопка со своим содержимым (иконка и текст). Оформление то же, что у текстовой [ActionButton]. */
@Composable
fun ActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()

    val base = when {
        !enabled -> colors.buttonPressed
        pressed -> colors.buttonPressed
        hovered -> colors.buttonHover
        else -> colors.button
    }
    Box(
        modifier
            .defaultMinSize(minWidth = dimens.buttonMinWidth, minHeight = dimens.buttonHeight)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .background(Brush.verticalGradient(listOf(base, base.copy(alpha = 0.85f))), AppShapes.Small)
            .border(dimens.borderWidth, if (hovered && enabled) colors.textMuted else colors.buttonBorder, AppShapes.Small)
            .padding(horizontal = dimens.spaceL, vertical = dimens.spaceXs),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
