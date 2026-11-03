package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Кружок с «!»: по клику над ним появляется подсказка с [text], клик мимо её закрывает.
 * Подходит для ограничений поля или правил, которые не хочется держать на экране постоянно.
 */
@Composable
fun InfoButton(text: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var shown by remember { mutableStateOf(false) }
    val color = if (hovered || shown) colors.accentGold else colors.textBeige
    Box(
        modifier
            .size(dimens.iconSize)
            .border(dimens.borderWidth, color, CircleShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Button) { shown = true },
        contentAlignment = Alignment.Center,
    ) {
        Text("!", style = AppTheme.typography.bodyBold, color = color)
        if (shown) {
            Popup(
                popupPositionProvider = AboveAnchor,
                onDismissRequest = { shown = false },
                properties = PopupProperties(focusable = false, dismissOnClickOutside = true),
            ) {
                Box(
                    Modifier
                        .widthIn(max = dimens.infoTipMaxWidth)
                        .background(colors.backgroundRaised)
                        .border(dimens.borderWidth, colors.border)
                        .padding(dimens.spaceS),
                ) {
                    DescriptionText(text)
                }
            }
        }
    }
}

/** Подсказка над кнопкой, правым краем к её правому краю, и не выходит за окно. */
private object AboveAnchor : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, maxX)
        val above = anchorBounds.top - popupContentSize.height
        val y = if (above >= 0) above else anchorBounds.bottom
        return IntOffset(x, y)
    }
}
