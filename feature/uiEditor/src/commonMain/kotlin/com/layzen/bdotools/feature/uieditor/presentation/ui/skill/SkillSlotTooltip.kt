package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.slots_tooltip_alt_hint
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Подсказка над навыком [skillNo] в слоте: название, а пока зажат Alt — полный [SkillTooltip].
 * Alt ([rememberAltPressed]) меняет тултип сразу, без движения курсора.
 */
@Composable
internal fun SkillSlotTooltip(skillNo: Int, skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    if (rememberAltPressed()) {
        SkillTooltip(skillNo, skills, onEvent)
        return
    }
    val skill = skills.skill(skillNo) ?: return
    Column {
        Text(skill.name(stringResource(Res.string.slots_skill_names_language)), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary)
        Text(stringResource(Res.string.slots_tooltip_alt_hint), style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
    }
}
