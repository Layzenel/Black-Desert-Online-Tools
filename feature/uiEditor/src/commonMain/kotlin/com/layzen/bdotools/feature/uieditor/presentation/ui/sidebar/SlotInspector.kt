package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.slotTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_selection
import com.layzen.bdotools.feature.uieditor.resources.slots_selection_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_selection_many
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_index
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_x
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_y
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Выбранное на холсте: у одного слота — координаты с точным вводом, у одного окна чата — ещё и размер,
 * у нескольких — сколько выбрано и отдаление групп. Ничего не выбрано — подсказка.
 */
@Composable
internal fun SlotInspector(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(stringResource(Res.string.slots_selection))
        val screen = state.loaded?.game?.screen
        val slot = state.selection.singleOrNull()?.takeIf { state.selectedChats.isEmpty() }
        val position = slot?.let { state.layout[it] }
        val window = state.selectedChats.singleOrNull()?.takeIf { state.selection.isEmpty() }
        val chat = window?.let { state.layout.chats[it] }
        when {
            slot != null && position != null && screen != null -> SlotCoordinates(slot, position, screen.crosshair, onEvent)
            window != null && chat != null && screen != null -> ChatInspector(window, chat, screen, onEvent)
            state.selectedCount > 1 -> {
                DescriptionText(stringResource(Res.string.slots_selection_many, state.selectedCount))
                GroupSpreadSliders(state, onEvent)
            }
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
