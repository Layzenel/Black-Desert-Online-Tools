package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppTheme

/** Подсказка [text] под [content], пока над ним курсор: как у кнопок панели инструментов. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoverTooltip(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = rememberBelowAnchor(),
        tooltip = {
            TooltipPanel {
                Text(
                    text,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.widthIn(max = AppTheme.dimens.tooltipMaxWidth),
                )
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

/**
 * Подсказка с любым содержимым [tooltip] (карточка навыка, предмета) под [content]: появляется сразу, пока над ним
 * курсор, и пропадает, как только курсор ушёл или элемент нажали (до следующего наведения). Ширину задаёт [tooltip].
 * [enabled] = `false` прячет подсказку, а [content] остаётся тем же: её можно выключать посреди жеста
 * или по клавише-модификатору, не пересоздавая элемент.
 */
@Composable
fun HoverTooltip(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(hovered) { if (!hovered) pressed = false }
    Box(
        modifier
            .hoverable(interaction)
            // Только смотрит на нажатие в ранней фазе, ничего не поглощая: жесты элемента работают как прежде
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) pressed = true
                    }
                }
            },
    ) {
        content()
        if (enabled && hovered && !pressed) {
            Popup(popupPositionProvider = rememberBelowAnchor(), properties = PopupProperties(focusable = false)) {
                TooltipPanel(tooltip)
            }
        }
    }
}

@Composable
private fun rememberBelowAnchor(): BelowAnchorPositionProvider {
    val gap = with(LocalDensity.current) { AppTheme.dimens.spaceXs.roundToPx() }
    return remember(gap) { BelowAnchorPositionProvider(gap) }
}

