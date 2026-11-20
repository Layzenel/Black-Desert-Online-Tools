package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem

/**
 * Трекер комбо в виде [Combo.overlayStyle]: один и тот же поверх игры и в предпросмотре на вкладке настройки.
 * Схема комбо — отдельное окно ([SchemaOverlay]). Пока трекер ведёт комбо под гневом Чёрного духа ([rage]) — на
 * чёрно-красной подложке с огнём ([RagePanel]).
 */
@Composable
internal fun ComboOverlayContent(
    combo: Combo,
    overlay: OverlayState,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    rage: Boolean = false,
) {
    when (combo.overlayStyle) {
        OverlayStyle.Next -> NextSkillsOverlay(overlay, skills, loadDetails, nextStepNote(combo, overlay), rage)
        OverlayStyle.Focus -> FocusOverlay(combo, overlay, skills, loadDetails, rage)
    }
}
