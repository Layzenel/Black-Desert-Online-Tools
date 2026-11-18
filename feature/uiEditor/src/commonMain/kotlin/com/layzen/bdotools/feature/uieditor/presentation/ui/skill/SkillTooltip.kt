package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.layzen.bdotools.composable.skill.SkillTooltipContent
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import org.jetbrains.compose.resources.stringResource

/**
 * Полный тултип навыка [skillNo] ([SkillTooltipContent]). Подробности скачиваются, когда тултип показан.
 * Навыка нет в справочнике — тултипа нет.
 */
@Composable
internal fun SkillTooltip(skillNo: Int, skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    val skill = skills.skill(skillNo) ?: return
    val language = stringResource(Res.string.slots_skill_names_language)
    LaunchedEffect(skillNo) { onEvent(SlotsEvent.RequestSkillDetails(skillNo)) }
    SkillTooltipContent(
        // Название — из справочника: в подробностях у «Мощи» оно с HTML-разметкой
        name = skill.name(language),
        icon = skills.icons[skill.icon],
        text = skills.details[skillNo]?.text(language),
        keys = skills.keyBindings,
        failed = skillNo in skills.detailsFailed,
    )
}
