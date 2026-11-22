package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Прямоугольная кнопка, как «Применить», «Сброс», «Выход». Подпись, доступность, действие и вид — в [state]:
 * [ActionButtonState.Neutral] — серая, [ActionButtonState.Positive] — бронзовая, [ActionButtonState.Negative] —
 * тёмно-красная.
 * [icon] — белый силуэт слева от подписи, цвет ему задаёт кнопка.
 */
@Composable
fun ActionButton(state: ActionButtonState, modifier: Modifier = Modifier, icon: Painter? = null) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val surface = when (state) {
        is ActionButtonState.Neutral -> neutralSurface(colors, dimens.borderWidth, state.enabled, hovered, pressed)
        is ActionButtonState.Positive -> if (state.enabled) {
            positiveSurface(colors, dimens.borderWidth, hovered, pressed)
        } else {
            neutralSurface(colors, dimens.borderWidth, enabled = false, hovered, pressed)
        }
        is ActionButtonState.Negative -> if (state.enabled) {
            negativeSurface(colors, dimens.borderWidth, hovered, pressed)
        } else {
            neutralSurface(colors, dimens.borderWidth, enabled = false, hovered, pressed)
        }
    }
    val textColor = if (state.enabled) colors.textButton else colors.textDisabled
    Row(
        modifier
            .defaultMinSize(minWidth = dimens.buttonMinWidth, minHeight = dimens.buttonHeight)
            .pointerHoverIcon(if (state.enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, indication = null, enabled = state.enabled, role = Role.Button, onClick = state.onClick)
            .then(surface)
            .padding(horizontal = dimens.spaceL, vertical = dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(icon, contentDescription = null, modifier = Modifier.size(dimens.menuActionIconSize), colorFilter = ColorFilter.tint(textColor))
        }
        Text(text = state.text, style = AppTheme.typography.button, color = textColor, maxLines = 1)
    }
}
