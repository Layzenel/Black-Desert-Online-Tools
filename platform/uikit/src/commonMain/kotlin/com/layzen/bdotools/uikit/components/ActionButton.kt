package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.class_agent
import com.layzen.bdotools.uikit.resources.class_archer
import com.layzen.bdotools.uikit.resources.class_berserker
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Прямоугольная кнопка, как «Применить», «Сброс», «Выход», — любое действие в приложении. Подпись, иконка, горячая
 * клавиша, доступность, действие и вид — в [state]: [ActionButtonState.Neutral] — серая,
 * [ActionButtonState.Toggle] — включатель режима, [ActionButtonState.Positive] — бронзовая,
 * [ActionButtonState.Negative] — тёмно-красная. В строке кнопок каждой — `Modifier.weight(1f)`, в столбце —
 * `Modifier.fillMaxWidth()`; содержимое стоит по центру. [ActionButtonState.tooltip] — подсказка при наведении.
 */
@Composable
fun ActionButton(state: ActionButtonState, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val surface = when (state) {
        is ActionButtonState.Neutral -> neutralSurface(colors, dimens.borderWidth, state.enabled, hovered, pressed)
        // включённый режим выглядит как кнопка под курсором
        is ActionButtonState.Toggle -> neutralSurface(colors, dimens.borderWidth, state.enabled, hovered || state.checked, pressed)
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
    val textColor = when {
        !state.enabled -> colors.textDisabled
        state is ActionButtonState.Toggle && state.checked -> colors.accentGold
        else -> colors.textButton
    }
    // без подписи — квадратная кнопка-иконка панели инструментов (ToolbarButton)
    val iconOnly = state.text.isEmpty() && state.icon != null
    // подсказка — вне ряда: лишний элемент ряда получал бы промежуток spacedBy, и кнопка под
    // курсором расширялась. Размер от modifier (weight, fillMaxWidth) доходит до ряда через propagateMinConstraints
    Box(modifier, propagateMinConstraints = true) {
        Row(
            Modifier
                .defaultMinSize(minWidth = if (iconOnly) dimens.buttonHeight else dimens.buttonMinWidth, minHeight = dimens.buttonHeight)
                .pointerHoverIcon(if (state.enabled) PointerIcon.Hand else PointerIcon.Default)
                .clickable(interaction, indication = null, enabled = state.enabled, role = Role.Button, onClick = state.onClick)
                .then(surface)
                .padding(horizontal = if (iconOnly) dimens.spaceXs else dimens.spaceL, vertical = dimens.spaceXs),
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceS, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.icon?.let { icon ->
                val size = if (iconOnly) dimens.iconSize else dimens.menuActionIconSize
                Image(icon, contentDescription = null, modifier = Modifier.size(size), colorFilter = ColorFilter.tint(textColor))
            }
            if (!iconOnly) Text(
                text = state.text,
                style = AppTheme.typography.button,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // в узкой кнопке подпись сжимается и обрезается, иконка и клавиша остаются целыми
                modifier = Modifier.weight(1f, fill = false),
            )
            state.hotkey?.let { Text(it, style = AppTheme.typography.caption, color = colors.textMuted, maxLines = 1) }
        }
        state.tooltip?.let { HoverHint(it, hovered && !pressed) }
    }
}

internal class ActionButtonPreviews {
    @Preview
    @Composable
    fun ActionButtonPreview() {
        val modifier = Modifier.requiredWidth(150.dp)
        val neutralActionButtonState = ActionButtonState.Neutral(text = "Neutral", icon = painterResource(Res.drawable.class_agent), enabled = true, onClick = {})
        val positiveActionButtonState = ActionButtonState.Positive(text = "Positive", icon = painterResource(Res.drawable.class_archer), enabled = true, onClick = {})
        val negativeActionButtonState = ActionButtonState.Negative(text = "Negative", icon = painterResource(Res.drawable.class_berserker), enabled = true, onClick = {})

        AppTheme {
            Column {
                ActionButton(modifier = modifier, state = neutralActionButtonState)
                ActionButton(modifier = modifier, state = positiveActionButtonState)
                ActionButton(modifier = modifier, state = negativeActionButtonState)
            }
        }
    }
}
