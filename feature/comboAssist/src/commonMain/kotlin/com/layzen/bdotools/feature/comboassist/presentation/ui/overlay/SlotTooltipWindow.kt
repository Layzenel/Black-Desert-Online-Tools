package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem

/**
 * Тултип умения, когда курсор задержался над слотом перезарядки [slots] в игре: та же карточка, что в редакторе.
 * Только экран и положение курсора, в игру не вмешивается. Есть только в настольной версии.
 */
@Composable
internal expect fun SlotTooltipWindow(
    slots: List<ScreenSlot>,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
)
