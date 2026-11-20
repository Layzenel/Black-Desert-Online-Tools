package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection

/** Окон поверх игры в браузере нет. */
@Composable
internal actual fun SlotShieldsWindow(slots: List<ScreenSlot>, protections: Map<Int, Set<SkillProtection>>, controls: Map<Int, Set<CrowdControl>>) = Unit
