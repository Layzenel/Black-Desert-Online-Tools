package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.menu_redo
import com.layzen.bdotools.feature.slots.resources.menu_undo
import com.layzen.bdotools.feature.slots.resources.slots_fit_canvas
import com.layzen.bdotools.feature.slots.resources.slots_redo
import com.layzen.bdotools.feature.slots.resources.slots_snap_distance
import com.layzen.bdotools.feature.slots.resources.slots_snap_distance_value
import com.layzen.bdotools.feature.slots.resources.slots_snap_to_slots
import com.layzen.bdotools.feature.slots.resources.slots_undo
import com.layzen.bdotools.feature.slots.resources.tool_fit
import com.layzen.bdotools.feature.slots.resources.tool_snap
import com.layzen.bdotools.uikit.components.ActionBar
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.components.ToolbarDivider
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.components.ValueText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource



/**
 * Панель над холстом: история правок, прилипание и вид холста. Выравнивание — в контекстном меню холста.
 * Работа с файлами — внизу меню слева.
 */
@Composable
internal fun EditorToolbar(state: SlotsState, viewport: CanvasViewport, onEvent: (SlotsEvent) -> Unit) {
    ActionBar {
        HistoryTools(state, onEvent)
        ToolbarDivider()
        SnapTools(state, onEvent)
        ToolbarDivider()
        ToolbarButton(painterResource(Res.drawable.tool_fit), stringResource(Res.string.slots_fit_canvas), onClick = viewport::reset)
    }
}

@Composable
private fun HistoryTools(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    ToolGroup {
        ToolbarButton(
            painterResource(Res.drawable.menu_undo),
            stringResource(Res.string.slots_undo),
            onClick = { onEvent(SlotsEvent.Undo) },
            enabled = state.history.isNotEmpty(),
        )
        ToolbarButton(
            painterResource(Res.drawable.menu_redo),
            stringResource(Res.string.slots_redo),
            onClick = { onEvent(SlotsEvent.Redo) },
            enabled = state.future.isNotEmpty(),
        )
    }
}

/** Выключатель прилипания; порог показан, только пока прилипание включено. */
@Composable
private fun SnapTools(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    ToolGroup {
        ToolbarButton(
            painterResource(Res.drawable.tool_snap),
            stringResource(Res.string.slots_snap_to_slots),
            onClick = { onEvent(SlotsEvent.SetSnapToSlots(!state.snapToSlots)) },
            selected = state.snapToSlots,
        )
        if (state.snapToSlots) {
            DescriptionText(stringResource(Res.string.slots_snap_distance))
            ValueSlider(
                state.snapDistance,
                { onEvent(SlotsEvent.SetSnapDistance(it)) },
                range = SlotsState.SNAP_DISTANCE_RANGE,
                modifier = Modifier.width(SlotsDimens.snapSliderWidth),
                showLabels = false,
            )
            ValueText(stringResource(Res.string.slots_snap_distance_value, state.snapDistance), color = AppTheme.colors.accentGold)
        }
    }
}

/** Плотная группа однотипных кнопок: между ними промежуток меньше, чем между группами. */
@Composable
private fun ToolGroup(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}
