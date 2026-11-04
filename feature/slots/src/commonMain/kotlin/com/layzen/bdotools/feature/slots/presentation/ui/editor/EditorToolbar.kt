package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_fit
import com.layzen.bdotools.feature.slots.resources.slots_legend_class_resource
import com.layzen.bdotools.feature.slots.resources.slots_legend_cooldown
import com.layzen.bdotools.feature.slots.resources.slots_legend_hotbar
import com.layzen.bdotools.feature.slots.resources.slots_legend_interface
import com.layzen.bdotools.feature.slots.resources.slots_legend_off_screen
import com.layzen.bdotools.feature.slots.resources.slots_mirror
import com.layzen.bdotools.feature.slots.resources.slots_reload
import com.layzen.bdotools.feature.slots.resources.slots_undo
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Над холстом: легенда цветов, действия над выбранными элементами и «Весь экран» — сброс масштаба холста. */
@Composable
internal fun EditorToolbar(state: SlotsState, onEvent: (SlotsEvent) -> Unit, onFit: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        LegendItem(SlotGroup.Cooldown.frameColor(), stringResource(Res.string.slots_legend_cooldown))
        LegendItem(SlotGroup.Hotbar.frameColor(), stringResource(Res.string.slots_legend_hotbar))
        LegendItem(SlotGroup.ClassResource.frameColor(), stringResource(Res.string.slots_legend_class_resource))
        LegendItem(SlotGroup.Interface.frameColor(), stringResource(Res.string.slots_legend_interface))
        LegendItem(AppTheme.colors.danger, stringResource(Res.string.slots_legend_off_screen))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        ActionButton(stringResource(Res.string.slots_mirror), onClick = { onEvent(SlotsEvent.Mirror) }, enabled = state.selection.isNotEmpty())
        ActionButton(stringResource(Res.string.slots_undo), onClick = { onEvent(SlotsEvent.Undo) }, enabled = state.history.isNotEmpty())
        ActionButton(stringResource(Res.string.slots_fit), onClick = onFit)
        ActionButton(stringResource(Res.string.slots_reload), onClick = { onEvent(SlotsEvent.Reload) })
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Box(Modifier.size(SlotsDimens.legendSwatch).border(SlotsDimens.slotBorder, color))
        Text(text, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
    }
}
