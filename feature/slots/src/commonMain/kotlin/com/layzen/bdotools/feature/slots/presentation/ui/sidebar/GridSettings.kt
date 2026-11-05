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
import com.layzen.bdotools.feature.slots.resources.slots_snap_step
import com.layzen.bdotools.feature.slots.resources.slots_snap_step_description
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource


/** Шаг линий сетки и шаг привязки — отдельно, чтобы можно было, например, видеть сетку 50 и ставить по 5. */
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
        SectionHeader(
            stringResource(Res.string.slots_snap_step),
            description = stringResource(Res.string.slots_snap_step_description),
        )
        ValueSlider(state.snapStep, { onEvent(SlotsEvent.SetSnapStep(it)) }, range = SlotsState.SNAP_STEP_RANGE)
    }
}
