package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.ChainLink
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.ForkBracket
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.StepArrow
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.CellNote
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Схема комбо в оверлее: колонки со стрелками, развилки в скобках — слева направо или, у [Combo.vertical],
 * сверху вниз (ветки развилки рядом). Пройденные шаги тусклые, последний — в светлой рамке, следующий — в золотой
 * и мягко пульсирует, невыбранные ветки и опциональные шаги бледные, сгоревшие перечёркнуты. Переходы плавные.
 * [onPress] — клик по иконке (тренировка); `null` — иконки не кликаются.
 * [tooltips] — тултип умения при наведении (в окне поверх игры его нет).
 */
@Composable
internal fun ComboSchema(
    combo: Combo,
    overlay: OverlayState,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    onPress: ((Int) -> Unit)?,
    tooltips: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val vertical = combo.vertical
    val planned = overlay.plan.mapNotNull { it.node?.id }.toSet()
    Flow(vertical, modifier, AppTheme.dimens.spaceXs) {
        combo.columns.forEachIndexed { column, col ->
            // пустые колонки оверлей пропускает: ни стрелки, ни ячеек
            if (overlay.nodeAt.keys.none { it.column == column }) return@forEachIndexed
            if (overlay.nodeAt.keys.any { it.column < column }) Connector(col.lanes.firstOrNull()?.firstOrNull()?.chain == true && col is ComboColumn.Single, vertical)
            val lanes: @Composable () -> Unit = {
                // ветки прижаты к началу: короткая ветка не съезжает к середине длинной
                Flow(!vertical, Modifier, AppTheme.dimens.spaceS, alignStart = true) {
                    col.lanes.forEachIndexed { lane, cells ->
                        val ids = cells.indices.mapNotNull { overlay.nodeAt[CellAt(column, lane, it)] }
                        val siblings = overlay.nodeAt.filterKeys { it.column == column }.values
                        val skipped = col is ComboColumn.Fork && ids.none { it in planned || it in overlay.done } && siblings.any { it in planned || it in overlay.done }
                        val laneAlpha by animateFloatAsState(if (skipped) SKIPPED_ALPHA else 1f, tween(FADE_MS))
                        Flow(vertical, Modifier.graphicsLayer { alpha = laneAlpha }, AppTheme.dimens.spaceXs) {
                            cells.forEachIndexed { index, cell ->
                                if (cell.skillNo == null) return@forEachIndexed
                                if (index > 0) Connector(cell.chain, vertical)
                                SchemaCell(combo, CellAt(column, lane, index), cell.skillNo, cell.chain, cell.optional, overlay, skills, loadDetails, onPress, tooltips)
                            }
                        }
                    }
                }
            }
            if (col is ComboColumn.Fork) {
                Flow(vertical, if (vertical) Modifier.width(IntrinsicSize.Min) else Modifier.height(IntrinsicSize.Min), 0.dp) {
                    ForkBracket(start = true, vertical = vertical)
                    lanes()
                    ForkBracket(start = false, vertical = vertical)
                }
            } else lanes()
        }
    }
}

/** Ряд или столбец с промежутком [gap]: схема строится одинаково в обеих ориентациях. [alignStart] — без центровки поперёк. */
@Composable
private fun Flow(column: Boolean, modifier: Modifier, gap: Dp, alignStart: Boolean = false, content: @Composable () -> Unit) {
    if (column) {
        Column(modifier.width(IntrinsicSize.Min), verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = if (alignStart) Alignment.Start else Alignment.CenterHorizontally) { content() }
    } else {
        Row(modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = if (alignStart) Alignment.Top else Alignment.CenterVertically) { content() }
    }
}

@Composable
private fun Connector(chain: Boolean, vertical: Boolean) = if (chain) ChainLink(vertical = vertical) else StepArrow(vertical = vertical)

@Composable
private fun SchemaCell(
    combo: Combo,
    at: CellAt,
    skillNo: Int,
    chain: Boolean,
    optional: Boolean,
    overlay: OverlayState,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    onPress: ((Int) -> Unit)?,
    tooltips: Boolean,
) {
    val id = overlay.nodeAt[at]
    val next = overlay.next?.node?.id == id && id != null
    val current = id in overlay.current
    val alpha by animateFloatAsState(
        when {
            next || current -> 1f
            id in overlay.done -> DONE_ALPHA
            optional -> SKIPPED_ALPHA
            else -> 1f
        },
        tween(FADE_MS),
    )
    val frame by animateColorAsState(
        when {
            next -> if (overlay.waitMs > 0) AppTheme.colors.badgeNew else AppTheme.colors.accentGold
            current -> AppTheme.colors.textBeige
            else -> AppTheme.colors.border
        },
        tween(FADE_MS),
    )
    val scale by animateFloatAsState(if (next) NEXT_SCALE else 1f, tween(FADE_MS))
    val pulse = if (next) rememberInfiniteTransition().animateFloat(1f, PULSE_MIN, infiniteRepeatable(tween(PULSE_MS), RepeatMode.Reverse)).value else 1f
    val item = skills[skillNo]
    val click = onPress?.let { press -> Modifier.pointerHoverIcon(PointerIcon.Hand).clickable { press(skillNo) } } ?: Modifier
    val icon: @Composable () -> Unit = {
        SkillIconView(
            item?.icon, ComboDimens.overlayIcon,
            click.graphicsLayer { this.alpha = alpha * pulse; scaleX = scale; scaleY = scale },
            frame = frame, frameWidth = if (next) ComboDimens.nextFrame else ComboDimens.frame,
            cooldownLeftMs = overlay.cooldownLeft[skillNo] ?: 0, cooldownMs = item?.skill?.cooldownMs ?: 0,
            crossed = id in overlay.burned,
        )
    }
    if (tooltips) SkillHover(item, loadDetails, cell = CellNote(combo.label(at), chain, optional)) { icon() } else icon()
}

private const val DONE_ALPHA = 0.35f
private const val SKIPPED_ALPHA = 0.4f
private const val NEXT_SCALE = 1.12f
private const val PULSE_MIN = 0.8f
private const val PULSE_MS = 700
private const val FADE_MS = 300
