package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.domain.tracker.ChainRecovery
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerEvent
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerSettings
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_chain_relaunch
import com.layzen.bdotools.feature.comboassist.resources.combo_chain_rescue
import com.layzen.bdotools.feature.comboassist.resources.combo_chain_skip
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_status_forward
import com.layzen.bdotools.feature.comboassist.resources.combo_status_jump
import com.layzen.bdotools.feature.comboassist.resources.combo_status_off
import com.layzen.bdotools.feature.comboassist.resources.combo_status_reset
import org.jetbrains.compose.resources.stringResource

/** Название умения на языке интерфейса или его номер, если умения нет в справочнике. */
@Composable
internal fun skillName(skills: Map<Int, SkillItem>, skillNo: Int?): String =
    skillNo?.let { skills[it]?.skill?.name(stringResource(Res.string.combo_names_language)) ?: "#$it" }.orEmpty()

/** Что случилось со связкой, одной фразой; `null` — связка не обрывалась. */
@Composable
internal fun chainText(chain: ChainRecovery?, skills: Map<Int, SkillItem>): String? = when (chain) {
    null -> null
    is ChainRecovery.Rescue -> stringResource(Res.string.combo_chain_rescue, skillName(skills, chain.entry))
    is ChainRecovery.Relaunch -> stringResource(Res.string.combo_chain_relaunch, skillName(skills, chain.launcher.skillNo))
    is ChainRecovery.Skip -> stringResource(Res.string.combo_chain_skip, chain.lost)
}

/** Сообщение о событии трекера; `null` у обычного шага и филлера. */
@Composable
internal fun eventText(event: TrackerEvent, skills: Map<Int, SkillItem>): String? = when (event) {
    is TrackerEvent.Off -> stringResource(Res.string.combo_status_off, skillName(skills, event.skillNo), event.count, TrackerSettings().offLimit)
    is TrackerEvent.Jump -> stringResource(Res.string.combo_status_jump)
    is TrackerEvent.ErrorForward -> stringResource(Res.string.combo_status_forward, skillName(skills, event.skillNo), event.skipped)
    is TrackerEvent.Reset -> stringResource(Res.string.combo_status_reset)
    is TrackerEvent.ChainTimeout, is TrackerEvent.Step, is TrackerEvent.Filler -> null
}

