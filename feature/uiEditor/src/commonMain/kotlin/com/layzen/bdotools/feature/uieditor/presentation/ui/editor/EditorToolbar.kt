package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.menu_redo
import com.layzen.bdotools.feature.uieditor.resources.menu_undo
import com.layzen.bdotools.feature.uieditor.resources.slots_redo
import com.layzen.bdotools.feature.uieditor.resources.slots_snap_distance
import com.layzen.bdotools.feature.uieditor.resources.slots_snap_distance_value
import com.layzen.bdotools.feature.uieditor.resources.slots_snap_step
import com.layzen.bdotools.feature.uieditor.resources.slots_snap_step_caption
import com.layzen.bdotools.feature.uieditor.resources.slots_snap_to_slots
import com.layzen.bdotools.feature.uieditor.resources.slots_undo
import com.layzen.bdotools.feature.uieditor.resources.tool_grid_step
import com.layzen.bdotools.feature.uieditor.resources.tool_snap
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
 * Панель над холстом: история правок, привязка к сетке и прилипание к слотам. Выравнивание — в контекстном
 * меню холста. Работа с файлами — внизу меню слева.
 */
@Composable
internal fun EditorToolbar(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    ActionBar {
        HistoryTools(state, onEvent)
        ToolbarDivider()
        ToggleWithSlider(
            icon = painterResource(Res.drawable.tool_grid_step),
            title = stringResource(Res.string.slots_snap_step),
            enabled = state.snapToGrid,
            onToggle = { onEvent(SlotsEvent.SetSnapToGrid(it)) },
            caption = stringResource(Res.string.slots_snap_step_caption),
            value = state.snapStep,
            range = SlotsState.SNAP_STEP_RANGE,
            onValueChange = { onEvent(SlotsEvent.SetSnapStep(it)) },
        )
        ToolbarDivider()
        ToggleWithSlider(
            icon = painterResource(Res.drawable.tool_snap),
            title = stringResource(Res.string.slots_snap_to_slots),
            enabled = state.snapToSlots,
            onToggle = { onEvent(SlotsEvent.SetSnapToSlots(it)) },
            caption = stringResource(Res.string.slots_snap_distance),
            value = state.snapDistance,
            range = SlotsState.SNAP_DISTANCE_RANGE,
            onValueChange = { onEvent(SlotsEvent.SetSnapDistance(it)) },
        )
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

/** Выключатель режима; его [value] в пикселях со слайдером показано, только пока режим включён. */
@Composable
private fun ToggleWithSlider(
    icon: Painter,
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    caption: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
) {
    ToolGroup {
        ToolbarButton(icon, title, onClick = { onToggle(!enabled) }, selected = enabled)
        if (enabled) {
            DescriptionText(caption)
            ValueSlider(value, onValueChange, range = range, modifier = Modifier.width(SlotsDimens.snapSliderWidth), showLabels = false)
            ValueText(stringResource(Res.string.slots_snap_distance_value, value), color = AppTheme.colors.accentGold)
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
