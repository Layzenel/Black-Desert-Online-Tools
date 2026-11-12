package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_slot_number_hint
import com.layzen.bdotools.uikit.components.HoverTooltip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.skill.KeyBindings
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillEffects
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillText
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Плашка справа у навыка в окне выбора: эффекты защиты | контроли. Колонка защиты всегда шириной на все её значки,
 * поэтому контроли у всех навыков стоят друг под другом. [effects] `null` — подробности ещё не скачались: пустая плашка.
 * [text] — тултип навыка на языке интерфейса для подсказок значков, [keys] — клавиши в нём.
 * [hidePveControls] — без контролей, которые недоступны в PvP. Слева — [slotNumber], номер слота перезарядки, где навык
 * уже стоит (колонка есть всегда, пустая у навыков не в слотах).
 */
@Composable
internal fun SkillEffectsPlate(slotNumber: Int?, effects: SkillEffects?, text: SkillText?, keys: KeyBindings, hidePveControls: Boolean) {
    Row(
        Modifier
            .background(AppTheme.colors.backgroundSurface)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
            .padding(SlotsDimens.effectPlatePadding)
            .height(SlotsDimens.effectIcon),
        horizontalArrangement = Arrangement.spacedBy(SlotsDimens.effectPlatePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(SlotsDimens.slotNumberWidth), contentAlignment = Alignment.Center) {
            if (slotNumber != null) {
                HoverTooltip(stringResource(Res.string.slots_skill_slot_number_hint)) {
                    Text(slotNumber.toString(), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
                }
            }
        }
        EffectDivider()
        EffectRow(Modifier.width(SlotsDimens.defenseColumnWidth)) { DefenseIcons(effects?.defenses.orEmpty(), text, keys) }
        EffectDivider()
        val controls = effects?.let { if (hidePveControls) it.pvpControls else it.controls }.orEmpty()
        EffectRow(Modifier.widthIn(min = SlotsDimens.controlColumnMinWidth)) { ControlIcons(controls, text, keys) }
    }
}
