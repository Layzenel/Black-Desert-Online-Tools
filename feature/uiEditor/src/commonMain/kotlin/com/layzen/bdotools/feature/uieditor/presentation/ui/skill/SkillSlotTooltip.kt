package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.slots_tooltip_alt_hint
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Подсказка над навыком [skillNo] в слоте: «название | эффекты защиты | контроли» (только те, что действуют в PvP;
 * черта — только перед непустой группой), а пока зажат Alt — полный [SkillTooltip].
 * Alt ([rememberAltPressed]) меняет тултип сразу, без движения курсора.
 */
@Composable
internal fun SkillSlotTooltip(skillNo: Int, skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    LaunchedEffect(skillNo) { onEvent(SlotsEvent.RequestSkillDetails(skillNo)) }
    if (rememberAltPressed()) {
        SkillTooltip(skillNo, skills, onEvent)
        return
    }
    val skill = skills.skill(skillNo) ?: return
    val language = stringResource(Res.string.slots_skill_names_language)
    val text = skills.details[skillNo]?.text(language)
    val effects = skills.effectsOf(skillNo)
    val defenses = effects?.defenses.orEmpty()
    val controls = effects?.pvpControls.orEmpty()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SlotsDimens.effectPlatePadding)) {
            Text(skill.name(language), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary)
            if (defenses.isNotEmpty()) {
                EffectDivider()
                EffectRow { DefenseIcons(defenses, text, skills.keyBindings) }
            }
            if (controls.isNotEmpty()) {
                EffectDivider()
                EffectRow { ControlIcons(controls, text, skills.keyBindings) }
            }
        }
        Text(stringResource(Res.string.slots_tooltip_alt_hint), style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
    }
}
