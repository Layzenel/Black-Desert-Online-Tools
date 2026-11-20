package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_fillers_description
import com.layzen.bdotools.feature.comboassist.resources.combo_fillers_search
import com.layzen.bdotools.feature.comboassist.resources.combo_section_fillers_count
import org.jetbrains.compose.resources.stringResource

/**
 * Филлеры комбо галочками ([SkillChecklistSection]): умения класса не из комбо и не опциональные, с перезарядкой и уроном,
 * которые можно начать сами.
 */
@Composable
internal fun FillersSection(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.edited ?: return
    val inCombo = combo.cells().mapNotNull { it.second.skillNo }.toSet()
    SkillChecklistSection(
        stringResource(Res.string.combo_section_fillers_count, combo.fillers.size),
        stringResource(Res.string.combo_fillers_description),
        stringResource(Res.string.combo_fillers_search),
        combo.fillers, state.skills,
        { it.skillNo !in inCombo && it.skillNo !in combo.pool && !it.skill.onlyAfter && it.skill.cooldownMs > 0 && it.damage != false },
        { skillNo, on -> onEvent(if (on) ComboEvent.AddFiller(skillNo) else ComboEvent.RemoveFiller(skillNo)) },
        loadDetails,
    )
}
