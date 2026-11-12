package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.uieditor.presentation.ui.skill.SkillIcon
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_slot_number_hint
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Навык в списке выбора: номер слота перезарядки, где он уже стоит ([slotNumber], колонка есть всегда),
 * иконка и название, клик — [onPick]. [onTooltip] говорит, показывать ли его тултип:
 * курсор над иконкой или над строкой с зажатым Alt ([altPressed]). Справа — плашка эффектов защиты и контролей
 * [effects] (плашка `SkillEffectsPlate`).
 */
@Composable
internal fun SkillChoice(
    name: String,
    slotNumber: Int?,
    icon: ImageBitmap?,
    effects: @Composable () -> Unit,
    altPressed: Boolean,
    onPick: () -> Unit,
    onTooltip: (Boolean) -> Unit,
) {
    val row = remember { MutableInteractionSource() }
    val iconArea = remember { MutableInteractionSource() }
    val rowHovered by row.collectIsHoveredAsState()
    val iconHovered by iconArea.collectIsHoveredAsState()
    val tooltip = iconHovered || (rowHovered && altPressed)
    DisposableEffect(tooltip) {
        onTooltip(tooltip)
        // Строка пропала из списка (поиск) — её тултип тоже
        onDispose { if (tooltip) onTooltip(false) }
    }
    Box(Modifier.hoverable(row)) {
        MenuItem(
            name,
            onClick = onPick,
            icon = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                    SlotNumber(slotNumber)
                    Box(Modifier.hoverable(iconArea)) { SkillIcon(icon) }
                }
            },
            badge = effects,
        )
    }
}

/** Номер слота золотом перед иконкой; колонка одной ширины у всех навыков, чтобы иконки стояли ровно. */
@Composable
private fun SlotNumber(number: Int?) {
    Box(Modifier.width(SlotsDimens.slotNumberWidth), contentAlignment = Alignment.Center) {
        if (number != null) {
            HoverTooltip(stringResource(Res.string.slots_skill_slot_number_hint)) {
                Text(number.toString(), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
            }
        }
    }
}
