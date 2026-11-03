package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Вариант из группы (как «Полноэкран. режим / Большое окно / Окно»):
 * квадратик-чекбокс и подпись. Выбранный — коричневая подложка и светлый текст.
 */
@Composable
fun OptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(
        modifier
            .heightIn(min = dimens.buttonHeight)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, null, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .background(if (selected) colors.backgroundSelected else colors.backgroundSurface)
            .border(
                dimens.borderWidth,
                when {
                    selected -> colors.borderSelected
                    hovered && enabled -> colors.buttonBorder
                    else -> colors.border
                },
            )
            .padding(horizontal = dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
    ) {
        Box(
            Modifier.size(dimens.checkboxSize).border(dimens.borderWidth, if (selected) colors.textBeige else colors.textDisabled),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(dimens.checkboxMarkSize).background(colors.danger))
        }
        Text(
            text,
            style = AppTheme.typography.bodyBold,
            color = if (selected) colors.textBeige else colors.textDisabled,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
    }
}
