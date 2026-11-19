package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem

/**
 * Тултип умения, когда курсор задержался над слотом перезарядки [slots] в игре: та же карточка, что в редакторе.
 * Пока курсор над щитом ([protections]) или значком контроля ([controls]) на слоте, тултипа умения нет: там своя
 * подсказка с названием. Только экран и положение курсора, в игру не вмешивается. Есть только в настольной версии.
 */
@Composable
internal expect fun SlotTooltipWindow(
    slots: List<ScreenSlot>,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    protections: Map<Int, Set<SkillProtection>>,
    controls: Map<Int, Set<CrowdControl>>,
)
