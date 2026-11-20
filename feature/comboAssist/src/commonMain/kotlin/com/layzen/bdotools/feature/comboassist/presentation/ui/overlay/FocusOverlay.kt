package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.tracker.PlannedStep
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.uikit.components.ProgressBar

import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Фокус-блок: крупная иконка «что жать сейчас», под ней клавиши в рамке постоянного размера, справа два следующих
 * шага, внизу полоса прогресса прохода и пометка игрока к шагу. Блок одной ширины: длинная комбинация клавиш и
 * длинное комбо его не растягивают. На входе в развилку в углу крупной иконки — первое умение другой ветки (его тоже
 * можно жать), у следующего шага, с которого начинается развилка, — синяя цифра с числом веток. Слева — опциональные
 * умения, которые можно вставить сейчас, столбиком в пунктирных рамках. Смена шага анимирована: текущий выезжает
 * справа, следующие и пометка проявляются, полоса прогресса и рамка готовности меняются плавно. Ориентацию комбо не
 * учитывает: блок и так маленький.
 */
@Composable
internal fun FocusOverlay(combo: Combo, overlay: OverlayState, skills: Map<Int, SkillItem>, loadDetails: suspend (Int) -> SkillDetails?, rage: Boolean = false) {
    val next = overlay.next ?: return
    OverlayPanel(rage) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            OptionalSkills(overlay, skills, ComboDimens.focusUpcomingIcon, vertical = true)
            Column(Modifier.width(ComboDimens.focusWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                    AnimatedContent(next, transitionSpec = { stepSwap() }, contentKey = ::stepKey) { step ->
                        CurrentStep(combo, step, overlay, skills, loadDetails)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                        overlay.plan.drop(1).take(UPCOMING_ALPHA.size).forEachIndexed { index, step ->
                            AnimatedContent(step, transitionSpec = { stepFade() }, contentKey = ::stepKey) {
                                UpcomingStep(combo, it, skills, Modifier.alpha(UPCOMING_ALPHA[index]))
                            }
                        }
                    }
                }
                val progress by animateFloatAsState(passProgress(overlay, next), tween(SWAP_MS))
                ProgressBar(progress)
                AnimatedContent(nextStepNote(combo, overlay), transitionSpec = { stepFade() }) { StepNote(it) }
            }
        }
    }
}

/** Крупная иконка текущего шага с другой веткой развилки в углу и клавишами в рамке постоянного размера под ней. */
@Composable
private fun CurrentStep(combo: Combo, next: PlannedStep, overlay: OverlayState, skills: Map<Int, SkillItem>, loadDetails: suspend (Int) -> SkillDetails?) {
    val item = skills[next.skillNo]
    val line = rememberNextCommandLine(overlay, skills, loadDetails)
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
        Box {
            val frame by animateColorAsState(nextFrameColor(overlay), tween(SWAP_MS))
            SkillIconView(item?.icon, ComboDimens.focusIcon, frame = frame, frameWidth = ComboDimens.nextFrame,
                cooldownLeftMs = overlay.cooldownLeft[next.skillNo] ?: 0, cooldownMs = item?.skill?.cooldownMs ?: 0)
            otherLaneStart(combo, next)?.let { skillNo ->
                SkillIconView(skills[skillNo]?.icon, ComboDimens.focusAltIcon, Modifier.align(Alignment.TopStart), frame = AppTheme.colors.accentBlue)
            }
        }
        // рамка клавиш постоянного размера: комбинация длиннее обрезается, а не растягивает блок
        Box(Modifier.size(ComboDimens.focusKeys).clipToBounds(), contentAlignment = Alignment.Center) { KeyCaps(line) }
    }
}

/** Следующий шаг: мелкая иконка, у связки — оранжевая рамка, перед развилкой — число веток. */
@Composable
private fun UpcomingStep(combo: Combo, step: PlannedStep, skills: Map<Int, SkillItem>, modifier: Modifier) {
    val frame = if (step.node?.chain == true) AppTheme.colors.textNote else AppTheme.colors.border
    Box(modifier) {
        SkillIconView(skills[step.skillNo]?.icon, ComboDimens.focusUpcomingIcon, frame = frame)
        val lanes = forkLanes(combo, step)
        if (lanes > 1) {
            Box(
                Modifier.align(Alignment.TopEnd).size(ComboDimens.forkBadge).background(AppTheme.colors.accentBlue, AppShapes.Pill),
                contentAlignment = Alignment.Center,
            ) { Text(lanes.toString(), style = AppTheme.typography.badge, color = AppTheme.colors.onBadge) }
        }
    }
}

/**
 * Доля прохода до шага [next]: пройденные шаги из колонок левее него к пройденным плюс оставшимся. Пройденное
 * в прошлом проходе не считается: когда комбо началось заново, полоса пустая.
 */
private fun passProgress(overlay: OverlayState, next: PlannedStep): Float {
    val column = next.node?.at?.column ?: return 0f
    val doneBefore = overlay.nodeAt.count { (at, id) -> at.column < column && id in overlay.done }
    val total = doneBefore + overlay.plan.size
    return if (total == 0) 0f else doneBefore.toFloat() / total
}

/** Число веток развилки, в которую входит [step]; 1 — шаг не начинает развилку. */
private fun forkLanes(combo: Combo, step: PlannedStep): Int {
    val at = step.node?.takeIf { it.isFork && it.at.index == 0 }?.at ?: return 1
    return combo.columns[at.column].lanes.size
}

/** Первое умение другой ветки развилки, если [step] в неё входит. Связанные развилки не учитывает: это лишь подсказка. */
private fun otherLaneStart(combo: Combo, step: PlannedStep): Int? {
    val at = step.node?.takeIf { it.isFork && it.at.index == 0 }?.at ?: return null
    return combo.columns[at.column].lanes.withIndex().filter { it.index != at.lane }.firstNotNullOfOrNull { it.value.firstOrNull()?.skillNo }
}

/** Шаг для анимации: смена одного и того же шага (время подсказки сдвинулось) анимацию не запускает. */
private fun stepKey(step: PlannedStep): Any = step.node?.id ?: step.skillNo

private val UPCOMING_ALPHA = listOf(0.85f, 0.55f)
