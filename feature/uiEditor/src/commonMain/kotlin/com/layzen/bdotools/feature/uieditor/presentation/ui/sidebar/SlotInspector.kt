package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.SlotId
import com.layzen.bdotools.feature.uieditor.domain.SlotPosition
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.slotTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_selection
import com.layzen.bdotools.feature.uieditor.resources.slots_selection_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_selection_many
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_index
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_x
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_y
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
        val crosshair = state.loaded?.game?.screen?.crosshair
        when {
            selected != null && position != null && crosshair != null -> SlotCoordinates(selected, position, crosshair, onEvent)
            state.selection.size > 1 -> DescriptionText(stringResource(Res.string.slots_selection_many, state.selection.size))
            else -> DescriptionText(stringResource(Res.string.slots_selection_hint))
        }
    }
}

/** Координаты показываются от прицела — на него игрок ориентируется в игре; хранятся от центра экрана. */
@Composable
private fun SlotCoordinates(id: SlotId, position: SlotPosition, crosshair: SlotPosition, onEvent: (SlotsEvent) -> Unit) {
    ValueText(slotTitle(id))
    DescriptionText(stringResource(Res.string.slots_slot_index, id.uiIndex))
    CoordinateRow(stringResource(Res.string.slots_slot_x), position.x - crosshair.x) {
        onEvent(SlotsEvent.SetPosition(id, position.copy(x = it + crosshair.x)))
    }
    CoordinateRow(stringResource(Res.string.slots_slot_y), position.y - crosshair.y) {
        onEvent(SlotsEvent.SetPosition(id, position.copy(y = it + crosshair.y)))
    }
}

@Composable
private fun CoordinateRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        NumberField(value, onValueChange, Modifier.width(SlotsDimens.coordinateFieldWidth), range = COORDINATE_RANGE)
    }
}
