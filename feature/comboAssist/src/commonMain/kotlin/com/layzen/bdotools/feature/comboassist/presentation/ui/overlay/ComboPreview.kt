package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.layzen.bdotools.feature.comboassist.domain.graph.ComboGraph
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.presentation.ComboViewModel
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem

/**
 * Схема комбо [combo] рядом с меню оверлея, пока курсор над ним в списке: видно, что это за комбо. Ориентация —
 * [vertical], выбранная в разделе схемы этого окна, а не собственная у комбо. Без трекера и тултипов; умения его
 * класса грузятся, если открыт другой класс ([ComboViewModel.previewSkills]). [keys] — под умениями их клавиши.
 */
@Composable
internal fun ComboPreview(combo: Combo, vertical: Boolean, keys: Boolean, viewModel: ComboViewModel) {
    val skills by produceState(emptyMap<Int, SkillItem>(), combo) { value = viewModel.previewSkills(combo) }
    // схема рисует только ячейки графа: без трекера граф строится здесь
    val shown = remember(combo, vertical) { combo.copy(vertical = vertical) }
    val overlay = remember(shown) { OverlayState(nodeAt = ComboGraph.of(shown).nodes.associate { it.at to it.id }) }
    SchemaOverlay(shown, overlay, skills, viewModel::details, inGame = true, keys = keys)
}
