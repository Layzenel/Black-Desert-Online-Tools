package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotPosition
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_selection
import com.layzen.bdotools.feature.slots.resources.slots_selection_hint
import com.layzen.bdotools.feature.slots.resources.slots_selection_many
import com.layzen.bdotools.feature.slots.resources.slots_slot_cooldown
import com.layzen.bdotools.feature.slots.resources.slots_slot_hotbar
import com.layzen.bdotools.feature.slots.resources.slots_slot_index
import com.layzen.bdotools.feature.slots.resources.slots_slot_x
import com.layzen.bdotools.feature.slots.resources.slots_slot_y
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.NumberField
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Насколько далеко от центра можно ввести координату руками, UI-px. */
private val COORDINATE_RANGE = -5000..5000

/** Координаты выбранного слота с точным вводом или подсказка, как выбрать слот. */
@Composable
internal fun SlotInspector(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(stringResource(Res.string.slots_selection))
        val selected = state.selection.singleOrNull()
        val position = selected?.let { state.layout[it] }
        when {
            selected != null && position != null -> SlotCoordinates(selected, position, onEvent)
            state.selection.size > 1 -> DescriptionText(stringResource(Res.string.slots_selection_many, state.selection.size))
            else -> DescriptionText(stringResource(Res.string.slots_selection_hint))
        }
    }
}

@Composable
private fun SlotCoordinates(id: SlotId, position: SlotPosition, onEvent: (SlotsEvent) -> Unit) {
    val title = if (id.group == SlotGroup.Cooldown) Res.string.slots_slot_cooldown else Res.string.slots_slot_hotbar
    ValueText(stringResource(title, id.number))
    DescriptionText(stringResource(Res.string.slots_slot_index, id.uiIndex))
    CoordinateRow(stringResource(Res.string.slots_slot_x), position.x) { onEvent(SlotsEvent.SetPosition(id, position.copy(x = it))) }
    CoordinateRow(stringResource(Res.string.slots_slot_y), position.y) { onEvent(SlotsEvent.SetPosition(id, position.copy(y = it))) }
}

@Composable
private fun CoordinateRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        NumberField(value, onValueChange, Modifier.width(SlotsDimens.coordinateFieldWidth), range = COORDINATE_RANGE)
    }
}
