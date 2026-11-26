package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem

/** В браузере окон поверх игры нет. */
@Composable
internal actual fun SlotTooltipWindow(
    slots: List<ScreenSlot>,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
) = Unit
