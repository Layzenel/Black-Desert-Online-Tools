package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.domain.AlignEdge
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.menu_redo
import com.layzen.bdotools.feature.slots.resources.menu_undo
import com.layzen.bdotools.feature.slots.resources.slots_align
import com.layzen.bdotools.feature.slots.resources.slots_align_anchor_hint
import com.layzen.bdotools.feature.slots.resources.slots_align_by_group
import com.layzen.bdotools.feature.slots.resources.slots_align_by_slot
import com.layzen.bdotools.feature.slots.resources.slots_align_group_hint
import com.layzen.bdotools.feature.slots.presentation.ui.slotTitle
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.theme.AppTheme
import androidx.compose.foundation.layout.padding
import com.layzen.bdotools.feature.slots.resources.slots_center_on_crosshair
import com.layzen.bdotools.feature.slots.resources.slots_distribute_horizontal
import com.layzen.bdotools.feature.slots.resources.slots_distribute_vertical
import com.layzen.bdotools.feature.slots.resources.slots_mirror
import com.layzen.bdotools.feature.slots.resources.slots_redo
import com.layzen.bdotools.feature.slots.resources.slots_snap_distance_value
import com.layzen.bdotools.feature.slots.resources.slots_snap_step
import com.layzen.bdotools.feature.slots.resources.slots_snap_to_slots
import com.layzen.bdotools.feature.slots.resources.slots_undo
import com.layzen.bdotools.feature.slots.resources.tool_align_center_x
import com.layzen.bdotools.feature.slots.resources.tool_center
import com.layzen.bdotools.feature.slots.resources.tool_chevron_right
import com.layzen.bdotools.feature.slots.resources.tool_distribute_horizontal
import com.layzen.bdotools.feature.slots.resources.tool_distribute_vertical
import com.layzen.bdotools.feature.slots.resources.tool_grid_step
import com.layzen.bdotools.feature.slots.resources.tool_mirror
import com.layzen.bdotools.feature.slots.resources.tool_snap
import com.layzen.bdotools.uikit.components.ActionMenuDivider
import com.layzen.bdotools.uikit.components.ActionMenuItem
import com.layzen.bdotools.uikit.components.ContextMenu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Сколько элементов нужно выбрать, чтобы их можно было выровнять или распределить. */
private const val ARRANGE_MIN = 2

/**
 * Меню правого клика по холсту: отмена и повтор; «Выровнять» (справа выезжает список выравниваний); распределение (клик распределяет,
 * справа выезжает промежуток между рамками),
 * центр по прицелу и отражение; «Шаг привязки» и «Связка слотов» (клик включает, при включённой справа выезжает слайдер).
 * Недоступные пункты серые; пункт действия сам закрывает меню, настройки — нет. [anchor] — элемент, по которому открыли
 * меню: выравнивание идёт по нему, без него — по рамке всех выбранных.
 */
@Composable
internal fun CanvasContextMenu(state: SlotsState, anchor: SlotId?, onDismiss: () -> Unit, onEvent: (SlotsEvent) -> Unit) {
    val selected = state.selection.size
    val alignAnchor = anchor?.takeIf { it in state.selection }
    fun run(event: SlotsEvent) = { onDismiss(); onEvent(event) }
    ContextMenu<ContextSubmenu>(
        expanded = true,
        onDismissRequest = onDismiss,
        submenu = { submenu ->
            when (submenu) {
                ContextSubmenu.Align -> {
                    // Сначала — по чему выравниваем: опора подсвечена на холсте золотой рамкой
                    DescriptionText(
                        if (alignAnchor != null) stringResource(Res.string.slots_align_anchor_hint, slotTitle(alignAnchor))
                        else stringResource(Res.string.slots_align_group_hint),
                        Modifier.padding(AppTheme.dimens.spaceM),
                    )
                    ActionMenuDivider()
                    AlignEdge.entries.forEach { edge ->
                        ActionMenuItem(stringResource(edge.title()), run(SlotsEvent.Align(edge, alignAnchor)), icon = painterResource(edge.icon()))
                    }
                }
                ContextSubmenu.DistributeHorizontal, ContextSubmenu.DistributeVertical ->
                    SubmenuSlider(state.distributeGap, SlotsState.DISTRIBUTE_GAP_RANGE) { onEvent(SlotsEvent.SetDistributeGap(it)) }
                ContextSubmenu.SnapDistance ->
                    SubmenuSlider(state.snapDistance, SlotsState.SNAP_DISTANCE_RANGE) { onEvent(SlotsEvent.SetSnapDistance(it)) }
                ContextSubmenu.SnapStep ->
                    SubmenuSlider(state.snapStep, SlotsState.SNAP_STEP_RANGE) { onEvent(SlotsEvent.SetSnapStep(it)) }
            }
        },
    ) { trigger ->
        ActionMenuItem(
            stringResource(Res.string.slots_undo),
            run(SlotsEvent.Undo),
            enabled = state.history.isNotEmpty(),
            icon = painterResource(Res.drawable.menu_undo),
        )
        ActionMenuItem(
            stringResource(Res.string.slots_redo),
            run(SlotsEvent.Redo),
            enabled = state.future.isNotEmpty(),
            icon = painterResource(Res.drawable.menu_redo),
        )
        ActionMenuDivider()
        val canAlign = selected >= ARRANGE_MIN
        ActionMenuItem(
            stringResource(Res.string.slots_align),
            onClick = {},
            modifier = if (canAlign) trigger(ContextSubmenu.Align) else Modifier,
            enabled = canAlign,
            icon = painterResource(Res.drawable.tool_align_center_x),
            trailingText = if (!canAlign) null
            else stringResource(if (alignAnchor != null) Res.string.slots_align_by_slot else Res.string.slots_align_by_group),
            trailingIcon = painterResource(Res.drawable.tool_chevron_right),
        )
        // Клик распределяет, справа выезжает промежуток между рамками
        val canDistribute = selected >= ARRANGE_MIN
        val gapText = stringResource(Res.string.slots_snap_distance_value, state.distributeGap)
        ActionMenuItem(
            stringResource(Res.string.slots_distribute_horizontal),
            run(SlotsEvent.Distribute(horizontal = true)),
            modifier = if (canDistribute) trigger(ContextSubmenu.DistributeHorizontal) else Modifier,
            enabled = canDistribute,
            icon = painterResource(Res.drawable.tool_distribute_horizontal),
            trailingText = gapText.takeIf { canDistribute },
            trailingIcon = painterResource(Res.drawable.tool_chevron_right),
        )
        ActionMenuItem(
            stringResource(Res.string.slots_distribute_vertical),
            run(SlotsEvent.Distribute(horizontal = false)),
            modifier = if (canDistribute) trigger(ContextSubmenu.DistributeVertical) else Modifier,
            enabled = canDistribute,
            icon = painterResource(Res.drawable.tool_distribute_vertical),
            trailingText = gapText.takeIf { canDistribute },
            trailingIcon = painterResource(Res.drawable.tool_chevron_right),
        )
        ActionMenuDivider()
        ActionMenuItem(
            stringResource(Res.string.slots_center_on_crosshair),
            run(SlotsEvent.CenterOnCrosshair),
            enabled = selected > 0,
            icon = painterResource(Res.drawable.tool_center),
        )
        ActionMenuItem(
            stringResource(Res.string.slots_mirror),
            run(SlotsEvent.Mirror),
            enabled = selected > 0,
            icon = painterResource(Res.drawable.tool_mirror),
        )
        ActionMenuDivider()
        // Порог показан, только пока связка включена
        ActionMenuItem(
            stringResource(Res.string.slots_snap_to_slots),
            onClick = { onEvent(SlotsEvent.SetSnapToSlots(!state.snapToSlots)) },
            modifier = if (state.snapToSlots) trigger(ContextSubmenu.SnapDistance) else Modifier,
            icon = painterResource(Res.drawable.tool_snap),
            trailingText = if (state.snapToSlots) stringResource(Res.string.slots_snap_distance_value, state.snapDistance) else null,
            trailingIcon = if (state.snapToSlots) painterResource(Res.drawable.tool_chevron_right) else null,
            toggled = state.snapToSlots,
        )
        // Шаг показан, только пока привязка включена
        ActionMenuItem(
            stringResource(Res.string.slots_snap_step),
            onClick = { onEvent(SlotsEvent.SetSnapToGrid(!state.snapToGrid)) },
            modifier = if (state.snapToGrid) trigger(ContextSubmenu.SnapStep) else Modifier,
            icon = painterResource(Res.drawable.tool_grid_step),
            trailingText = if (state.snapToGrid) stringResource(Res.string.slots_snap_distance_value, state.snapStep) else null,
            trailingIcon = if (state.snapToGrid) painterResource(Res.drawable.tool_chevron_right) else null,
            toggled = state.snapToGrid,
        )
    }
}
