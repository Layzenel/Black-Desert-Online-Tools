package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Выбор цвета из готовых: квадраты всегда в одну строку (не хватает места — сжимаются поровну), первый —
 * цвет по умолчанию [defaultColor] (выбор `null`), за ним [colors].
 * Выбранный — в золотой рамке, наведённый — в светлой. [defaultLabel] — подсказка и описание первого квадрата.
 *
 * @param selected индекс в [colors] или `null` — выбран цвет по умолчанию
 */
@Composable
fun ColorPalette(
    colors: List<Color>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
    defaultColor: Color,
    defaultLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        // TooltipBox не передаёт weight в Row, поэтому вес — у обёртки
        Box(Modifier.weight(1f, fill = false)) {
            HoverTooltip(defaultLabel) {
                Swatch(defaultColor, selected == null, defaultLabel) { onSelect(null) }
            }
        }
        colors.forEachIndexed { index, color ->
            Box(Modifier.weight(1f, fill = false)) { Swatch(color, selected == index, null) { onSelect(index) } }
        }
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, description: String?, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val frame = when {
        selected -> AppTheme.colors.accentGold
        hovered -> AppTheme.colors.textPrimary
        else -> AppTheme.colors.border
    }
    Box(
        Modifier
            .sizeIn(maxWidth = AppTheme.dimens.swatchSize, maxHeight = AppTheme.dimens.swatchSize)
            .aspectRatio(1f)
            .border(AppTheme.dimens.accentLineWidth, frame)
            .padding(AppTheme.dimens.accentLineWidth + AppTheme.dimens.borderWidth)
            .background(color)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
    )
}
