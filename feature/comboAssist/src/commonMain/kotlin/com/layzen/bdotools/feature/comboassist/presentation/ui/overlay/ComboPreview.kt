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
 * Схема комбо [combo] рядом с меню оверлея, пока курсор над ним в списке: видно, что это за комбо. Без трекера
 * и тултипов; умения его класса грузятся, если открыт другой класс ([ComboViewModel.previewSkills]).
 */
@Composable
internal fun ComboPreview(combo: Combo, viewModel: ComboViewModel) {
    val skills by produceState(emptyMap<Int, SkillItem>(), combo) { value = viewModel.previewSkills(combo) }
    // схема рисует только ячейки графа: без трекера граф строится здесь
    val overlay = remember(combo) { OverlayState(nodeAt = ComboGraph.of(combo).nodes.associate { it.at to it.id }) }
    SchemaOverlay(combo, overlay, skills, viewModel::details, inGame = true)
}
