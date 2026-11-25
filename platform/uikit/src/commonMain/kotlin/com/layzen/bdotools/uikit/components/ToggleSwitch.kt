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
import androidx.compose.ui.unit.dp
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Переключатель-пилюля: включён — золотой с кремовым кружком справа.
 *
 * @param enabled `false` — недоступен: не нажимается, подпись серая (почему — пиши рядом)
 */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(remember { MutableInteractionSource() }, null, enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
    ) {
        Box(
            Modifier
                .size(width = dimens.toggleWidth, height = dimens.toggleHeight)
                .background(if (checked) colors.toggleOn else colors.toggleTrack, AppShapes.Pill)
                .border(dimens.borderWidth, if (checked) colors.windowOutline else colors.toggleTrackBorder, AppShapes.Pill)
                .padding(if (checked) 0.dp else dimens.spaceXxs),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            // включённый кружок во всю высоту дорожки, как в игре
            Box(
                Modifier.size(if (checked) dimens.toggleHeight else dimens.toggleThumbSize)
                    .background(if (checked) colors.toggleThumbOn else colors.toggleThumbOff, AppShapes.Pill),
            )
        }
        if (label != null || description != null) {
            Column {
                if (label != null) Text(label, style = AppTheme.typography.sectionTitle, color = if (enabled) colors.textBeige else colors.textDisabled)
                if (description != null) DescriptionText(description)
            }
        }
    }
}
