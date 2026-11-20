package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.CrossIcon
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.StepArrow
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_move_left
import com.layzen.bdotools.feature.comboassist.resources.combo_move_right
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_add
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_description
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_search
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_title
import com.layzen.bdotools.feature.comboassist.resources.combo_remove
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.OptionsMenu
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Опциональные умения комбо ([com.layzen.bdotools.feature.comboassist.domain.combo.Combo.pool]) в редакторе, под
 * правкой ячейки: иконки в том порядке, в каком их покажет трекер; у каждой — крестик и стрелки «левее» / «правее».
 * «+» в конце открывает умения класса с поиском: не из комбо и не филлеры, с перезарядкой, которые можно начать сами.
 */
@Composable
internal fun OptionalSkillsEditor(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.edited ?: return
    val language = stringResource(Res.string.combo_names_language)
    SectionHeader(stringResource(Res.string.combo_optional_title), description = stringResource(Res.string.combo_optional_description))
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        combo.pool.forEach { skillNo -> OptionalSkill(skillNo, state.skills[skillNo], onEvent, loadDetails) }
        var open by remember { mutableStateOf(false) }
        Box {
            AddCellSlot(stringResource(Res.string.combo_optional_add)) { open = true }
            val inCombo = combo.cells().mapNotNull { it.second.skillNo }.toSet()
            val candidates = state.skills.values
                .filter { it.skillNo !in inCombo && it.skillNo !in combo.fillers && it.skillNo !in combo.pool && !it.skill.onlyAfter && it.skill.cooldownMs > 0 }
                .sortedBy { it.skill.name(language).lowercase() }
            OptionsMenu(
                open, { open = false }, candidates, null, { onEvent(ComboEvent.AddPool(it.skillNo)) }, { it.skill.name(language) },
                icon = { SkillIconView(it.icon, ComboDimens.paletteIcon) },
                searchPlaceholder = stringResource(Res.string.combo_optional_search),
            )
        }
    }
}

/** Одно опциональное умение: иконка с крестиком, под ней — стрелки сдвига. */
@Composable
private fun OptionalSkill(skillNo: Int, item: SkillItem?, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Box {
            SkillHover(item, loadDetails) { SkillIconView(item?.icon, ComboDimens.editorIcon) }
            HoverTooltip(stringResource(Res.string.combo_remove), Modifier.align(Alignment.TopEnd)) {
                Box(
                    Modifier.size(ComboDimens.removeButton).background(AppTheme.colors.backgroundDeep)
                        .pointerHoverIcon(PointerIcon.Hand).clickable { onEvent(ComboEvent.RemovePool(skillNo)) },
                    contentAlignment = Alignment.Center,
                ) { CrossIcon() }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
            SmallButton(stringResource(Res.string.combo_move_left), { onEvent(ComboEvent.MovePool(skillNo, -1)) }) { StepArrow(Modifier.rotate(HALF_TURN)) }
            SmallButton(stringResource(Res.string.combo_move_right), { onEvent(ComboEvent.MovePool(skillNo, 1)) }) { StepArrow() }
        }
    }
}

private const val HALF_TURN = 180f
