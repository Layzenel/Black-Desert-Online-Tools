package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Переключатель-пилюля: включён — золотой с кремовым кружком справа. */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(remember { MutableInteractionSource() }, null, role = Role.Switch) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
    ) {
        Box(
            Modifier
                .size(width = dimens.toggleWidth, height = dimens.toggleHeight)
                .background(if (checked) colors.accentGold else colors.backgroundItem, AppShapes.Pill)
                .border(dimens.borderWidth, if (checked) colors.accentGoldPale else colors.buttonBorder, AppShapes.Pill)
                .padding(dimens.spaceXxs),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(Modifier.size(dimens.toggleThumbSize).background(if (checked) colors.textPrimary else colors.textDisabled, AppShapes.Pill))
        }
        if (label != null || description != null) {
            Column {
                if (label != null) Text(label, style = AppTheme.typography.sectionTitle, color = colors.textBeige)
                if (description != null) DescriptionText(description)
            }
        }
    }
}
