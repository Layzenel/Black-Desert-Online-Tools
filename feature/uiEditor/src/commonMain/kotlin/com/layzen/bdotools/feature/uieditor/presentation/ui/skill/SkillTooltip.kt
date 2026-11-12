package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.slots_tooltip_failed
import com.layzen.bdotools.feature.uieditor.resources.slots_tooltip_loading
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Полный тултип навыка [skillNo], как в окне умений игры: иконка в золотистой рамке и название над разделителем,
 * затем блоки описания, требований с перезарядкой, команды и эффектов ([SkillTooltipBody]). Рамку всего тултипа
 * рисует `TooltipPanel`. Подробности скачиваются, когда тултип показан. Навыка нет в справочнике — тултипа нет.
 */
@Composable
internal fun SkillTooltip(skillNo: Int, skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    val skill = skills.skill(skillNo) ?: return
    val language = stringResource(Res.string.slots_skill_names_language)
    LaunchedEffect(skillNo) { onEvent(SlotsEvent.RequestSkillDetails(skillNo)) }
    val text = skills.details[skillNo]?.text(language)
    Column(
        Modifier.width(SlotsDimens.skillTooltipWidth).padding(vertical = AppTheme.dimens.spaceXs),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            Box(Modifier.border(AppTheme.dimens.borderWidth, AppTheme.colors.accentGoldPale).padding(AppTheme.dimens.tooltipFrameGap)) {
                SkillIcon(skills.icons[skill.icon], SlotsDimens.skillTooltipIcon)
            }
            // Название — из справочника: в подробностях у «Мощи» оно с HTML-разметкой
            Text(skill.name(language), style = AppTheme.typography.windowTitle, color = AppTheme.colors.textPrimary)
        }
        SectionDivider()
        when {
            text != null -> SkillTooltipBody(text, skills.keyBindings)
            skillNo in skills.detailsFailed -> WarningText(stringResource(Res.string.slots_tooltip_failed))
            else -> DescriptionText(stringResource(Res.string.slots_tooltip_loading))
        }
    }
}
