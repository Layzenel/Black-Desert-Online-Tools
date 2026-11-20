package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.ChainLink
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.ForkBracket
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.ForkIcon
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.PlusIcon
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.CrossIcon
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.StepArrow
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.CellNote
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_add_lane
import com.layzen.bdotools.feature.comboassist.resources.combo_remove
import com.layzen.bdotools.feature.comboassist.resources.combo_add_cell
import com.layzen.bdotools.feature.comboassist.resources.combo_grow_lane
import com.layzen.bdotools.feature.comboassist.resources.combo_make_fork
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.onSecondaryClick
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Холст редактора: колонки слева направо, развилки в скобках, в углу ячейки — её подпись («4.1») и крестик удаления.
 * В конце комбо и веток — «+», под обычной колонкой — кнопка «сделать развилкой», под развилкой — «+ ветка».
 * Ячейку перетаскивают на другую: к левому или правому краю — встаёт перед или после неё, к верхнему или нижнему —
 * новой веткой над или под ней; синяя черта в промежутке показывает, куда. Ячейку ветки можно бросить слева или справа
 * от неё самой — она выйдет из развилки. ПКМ по ячейке удаляет её. Выбранная ячейка в золотой
 * рамке, цель перетаскивания — в синей, продолжение связки не после своего запуска — в красной.
 */
@Composable
internal fun ComboCanvas(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.edited ?: return
    val bounds = remember(combo.columns) { mutableStateMapOf<CellAt, Rect>() }
    var drag by remember { mutableStateOf<CellDrag?>(null) }
    val slop = with(LocalDensity.current) { ComboDimens.dropSlop.toPx() }
    val dragTarget = drag?.let { d -> CellDrop.find(bounds, d.from, combo.columns[d.from.column] is ComboColumn.Fork, d.pointer, slop) }
    val dragging = CanvasDrag(
        current = drag,
        target = dragTarget,
        onStart = { at -> bounds[at]?.let { drag = CellDrag(at, it.center, Offset.Zero) } },
        onMove = { amount -> drag = drag?.let { it.copy(offset = it.offset + amount) } },
        onEnd = {
            val from = drag?.from
            if (from != null && dragTarget != null) onEvent(ComboEvent.MoveCell(from, dragTarget.at, dragTarget.side))
            drag = null
        },
        onCancel = { drag = null },
        place = { at, rect -> bounds[at] = rect },
    )
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
        combo.columns.forEachIndexed { column, col ->
            // между колонками — стрелка или звено связки. Звено одиночной ячейки — здесь, вне колонки: иначе кнопка «развилка»
            // под колонкой встаёт по центру звена и ячейки, а не под ячейкой. У развилки звено своей ветки — в самой ветке
            val singleChain = col is ComboColumn.Single && col.lanes.first().firstOrNull()?.chain == true
            if (singleChain) Box(Modifier.height(ComboDimens.editorIcon), contentAlignment = Alignment.Center) { ChainLink() }
            else if (column > 0 && col.lanes.none { it.firstOrNull()?.chain == true }) StepArrow()
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                // пустое место над колонкой той же высоты, что кнопки под ней: центр колонки — на ячейках, и «+» рядом с ней
                Spacer(Modifier.height(ComboDimens.gapButton))
                val lanes: @Composable () -> Unit = {
                    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                        col.lanes.forEachIndexed { lane, cells ->
                            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.Bottom) {
                                cells.forEachIndexed { index, cell ->
                                    if (index > 0 || (cell.chain && !singleChain)) Box(Modifier.height(ComboDimens.editorIcon), contentAlignment = Alignment.Center) { if (cell.chain) ChainLink() else StepArrow() }
                                    EditorCell(combo, CellAt(column, lane, index), cell, state, dragging, onEvent, loadDetails)
                                }
                                if (col is ComboColumn.Fork) AddCellSlot(stringResource(Res.string.combo_grow_lane)) { onEvent(ComboEvent.GrowLane(column, lane)) }
                            }
                        }
                    }
                }
                if (col is ComboColumn.Fork) {
                    Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                        ForkBracket(start = true)
                        // отступ от скобки: её верхняя и нижняя черта не заходят под ячейки
                        Box(Modifier.padding(vertical = AppTheme.dimens.spaceS)) { lanes() }
                        ForkBracket(start = false)
                    }
                    SmallButton(stringResource(Res.string.combo_add_lane), { onEvent(ComboEvent.AddLane(column)) }) { PlusIcon() }
                } else {
                    lanes()
                    SmallButton(stringResource(Res.string.combo_make_fork), { onEvent(ComboEvent.ToFork(column)) }) { ForkIcon() }
                }
            }
            if (column == combo.columns.lastIndex) {
                AddCellSlot(stringResource(Res.string.combo_add_cell)) { onEvent(ComboEvent.InsertColumn(column + 1)) }
            }
        }
    }
}

@Composable
private fun EditorCell(
    combo: Combo,
    at: CellAt,
    cell: ComboCell,
    state: ComboState,
    drag: CanvasDrag,
    onEvent: (ComboEvent) -> Unit,
    loadDetails: suspend (Int) -> SkillDetails?,
) {
    val selected = state.selection == at
    val dragged = drag.current?.from == at
    val frame = when {
        selected -> AppTheme.colors.accentGold
        at in state.problems -> AppTheme.colors.danger
        else -> AppTheme.colors.border
    }
    val start by rememberUpdatedState(drag.onStart)
    val move by rememberUpdatedState(drag.onMove)
    val end by rememberUpdatedState(drag.onEnd)
    val cancel by rememberUpdatedState(drag.onCancel)
    // цель выше соседей: черта места стоит снаружи ячейки, поверх стрелки
    Box(Modifier.zIndex(if (dragged || drag.target?.at == at) 1f else 0f)) {
        val item = cell.skillNo?.let { state.skills[it] }
        SkillHover(item, loadDetails, cell = CellNote(combo.label(at), cell.chain)) {
            SkillIconView(
                item?.icon, ComboDimens.editorIcon,
                Modifier
                    .onGloballyPositioned { drag.place(at, it.boundsInRoot()) }
                    .graphicsLayer {
                        val offset = drag.current?.takeIf { it.from == at }?.offset ?: Offset.Zero
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .alpha(if (dragged) DRAGGED_ALPHA else 1f)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .onSecondaryClick { onEvent(ComboEvent.RemoveAt(at)) }
                    .pointerInput(at) {
                        detectDragGestures(
                            onDragStart = { start(at) },
                            onDrag = { change, amount -> change.consume(); move(amount) },
                            onDragEnd = { end() },
                            onDragCancel = { cancel() },
                        )
                    }
                    .clickable { onEvent(ComboEvent.SelectCell(at)) },
                frame = frame, frameWidth = if (selected) ComboDimens.nextFrame else ComboDimens.frame,
            )
        }
        drag.target?.takeIf { it.at == at }?.let { DropMark(it.side, Modifier.matchParentSize()) }
        // подписи над иконкой едут вместе с ней, когда ячейку тянут
        val shift = Modifier.graphicsLayer {
            val offset = drag.current?.takeIf { it.from == at }?.offset ?: Offset.Zero
            translationX = offset.x
            translationY = offset.y
        }
        val backdrop = AppTheme.colors.backgroundDeep.copy(alpha = LABEL_BACKDROP_ALPHA)
        // номер ячейки — в углу иконки на тёмной подложке; не мешает тянуть и кликать ячейку
        Text(
            combo.label(at),
            shift.padding(ComboDimens.nextFrame).background(backdrop).padding(horizontal = AppTheme.dimens.spaceXxs),
            style = AppTheme.typography.caption,
            color = if (selected) AppTheme.colors.accentGold else AppTheme.colors.textPrimary,
        )
        // крестик в правом углу удаляет ячейку (как ПКМ по ней)
        // обёртка с выравниванием: HoverTooltip модификатор позиции в Box не передаёт
        Box(Modifier.align(Alignment.TopEnd)) {
            HoverTooltip(stringResource(Res.string.combo_remove)) {
                Box(
                    shift.padding(ComboDimens.nextFrame).size(ComboDimens.removeButton).background(backdrop)
                        .pointerHoverIcon(PointerIcon.Hand).clickable { onEvent(ComboEvent.RemoveAt(at)) },
                    contentAlignment = Alignment.Center,
                ) { CrossIcon() }
            }
        }
    }
}

private const val DRAGGED_ALPHA = 0.85f

/** Подложка номера ячейки: иконку под ней видно. */
private const val LABEL_BACKDROP_ALPHA = 0.7f
