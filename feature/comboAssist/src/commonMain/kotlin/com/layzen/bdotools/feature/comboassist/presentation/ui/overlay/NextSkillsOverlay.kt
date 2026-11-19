package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.StepArrow
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Оверлей поверх игры: карточка «что жать сейчас» с клавишами ([NextSkillCard]) и два следующих умения
 * в ряд на подложке тултипа игры, каждое бледнее предыдущего; пока ждёшь перезарядку — филлер. Под ними — пометка
 * игрока к шагу [note].
 */
@Composable
internal fun NextSkillsOverlay(
    overlay: OverlayState,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    note: String,
) {
    val upcoming = overlay.plan.drop(1).take(UPCOMING).map { it.skillNo }
    TooltipPanel {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
                AnimatedContent(
                    overlay.next?.skillNo,
                    transitionSpec = { (slideInHorizontally(tween(SWAP_MS)) { it / SLIDE_SHARE } + fadeIn(tween(SWAP_MS))) togetherWith fadeOut(tween(SWAP_MS)) },
                ) { skillNo -> if (skillNo != null) NextSkillCard(overlay, skills, loadDetails) }
                upcoming.forEachIndexed { index, skillNo ->
                    StepArrow()
                    UpcomingSkill(skillNo, overlay, skills, Modifier.alpha(UPCOMING_ALPHA[index]))
                }
                AnimatedVisibility(overlay.filler != null, enter = fadeIn(tween(SWAP_MS)), exit = fadeOut(tween(SWAP_MS))) {
                    overlay.filler?.let { FillerCard(it, skills) }
                }
            }
            StepNote(note)
        }
    }
}

/** Следующее умение: иконка с перезарядкой, без названия — так оверлей компактнее. */
@Composable
private fun UpcomingSkill(skillNo: Int, overlay: OverlayState, skills: Map<Int, SkillItem>, modifier: Modifier) {
    val item = skills[skillNo]
    SkillIconView(item?.icon, ComboDimens.upcomingIcon, modifier, cooldownLeftMs = overlay.cooldownLeft[skillNo] ?: 0, cooldownMs = item?.skill?.cooldownMs ?: 0)
}

private const val UPCOMING = 2
private val UPCOMING_ALPHA = listOf(0.85f, 0.6f)
private const val SWAP_MS = 250
private const val SLIDE_SHARE = 4
