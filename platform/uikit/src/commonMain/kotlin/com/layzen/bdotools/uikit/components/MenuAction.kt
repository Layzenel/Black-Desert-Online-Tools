package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Действие-пункт: иконка, подпись в одну строку и горячая клавиша справа. Ставится в меню слева на странице
 * [ToolPage] или строкой под содержимым (тогда каждому пункту — `Modifier.weight(1f)`).
 * Подпись не переносится: длинная обрезается многоточием, поэтому её лучше делать короткой.
 * [selected] — включённый режим (коричневая подложка с рамкой, золотой текст), [enabled] = `false` — серый текст.
 * [icon] — белый силуэт, цвет ему задаёт пункт.
 */
@Composable
fun MenuAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    hotkey: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val textColor = when {
        !enabled -> colors.textDisabled
        selected -> colors.accentGold
        else -> colors.textPrimary
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(dimens.menuActionHeight)
            .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .background(
                when {
                    selected -> colors.backgroundSelected
                    hovered && enabled -> colors.backgroundItem
                    else -> colors.backgroundRaised
                },
            )
            .border(dimens.borderWidth, if (selected) colors.borderSelected else colors.border)
            .padding(horizontal = dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
    ) {
        if (icon != null) {
            Image(icon, contentDescription = null, modifier = Modifier.size(dimens.menuActionIconSize), colorFilter = ColorFilter.tint(textColor))
        }
        Text(
            text,
            style = AppTheme.typography.bodyBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (hotkey != null) Text(hotkey, style = AppTheme.typography.caption, color = colors.textMuted, maxLines = 1)
    }
}
