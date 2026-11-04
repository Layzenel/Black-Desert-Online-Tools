package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.GridStep
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_grid
import com.layzen.bdotools.feature.slots.resources.slots_grid_off
import com.layzen.bdotools.feature.slots.resources.slots_grid_size
import com.layzen.bdotools.feature.slots.resources.slots_snap
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Шаг сетки и привязка к ней. */
@Composable
internal fun GridSettings(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(stringResource(Res.string.slots_grid))
        OptionGroup(
            options = GridStep.entries,
            selected = state.grid,
            onSelect = { onEvent(SlotsEvent.SetGrid(it)) },
            label = { if (it == GridStep.Off) stringResource(Res.string.slots_grid_off) else stringResource(Res.string.slots_grid_size, it.size) },
        )
        ToggleSwitch(
            checked = state.snap,
            onCheckedChange = { onEvent(SlotsEvent.SetSnap(it)) },
            label = stringResource(Res.string.slots_snap),
        )
    }
}
