package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.domain.BuiltInLayout
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.PresetRef
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_preset_arc
import com.layzen.bdotools.feature.slots.resources.slots_preset_block
import com.layzen.bdotools.feature.slots.resources.slots_preset_game
import com.layzen.bdotools.feature.slots.resources.slots_preset_sides
import com.layzen.bdotools.feature.slots.resources.slots_preset_two_rows
import com.layzen.bdotools.feature.slots.resources.slots_presets
import com.layzen.bdotools.feature.slots.resources.slots_presets_description
import com.layzen.bdotools.feature.slots.resources.slots_user_presets
import com.layzen.bdotools.feature.slots.resources.slots_user_presets_empty
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Раскладка из файла игры, готовые пресеты и пресеты пользователя. */
@Composable
internal fun PresetList(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        SectionHeader(stringResource(Res.string.slots_presets), description = stringResource(Res.string.slots_presets_description))
        MenuItem(
            stringResource(Res.string.slots_preset_game),
            onClick = { onEvent(SlotsEvent.UseGameLayout) },
            selected = state.activePreset == PresetRef.Game,
        )
        BuiltInLayout.entries.forEach { layout ->
            MenuItem(
                stringResource(layout.title),
                onClick = { onEvent(SlotsEvent.UseBuiltIn(layout)) },
                selected = state.activePreset == PresetRef.BuiltIn(layout),
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        SectionHeader(stringResource(Res.string.slots_user_presets))
        if (state.presets.isEmpty()) DescriptionText(stringResource(Res.string.slots_user_presets_empty))
        state.presets.forEach { preset ->
            // Крестик удаления — внутри пункта, справа, на месте плашки
            MenuItem(
                preset.name,
                onClick = { onEvent(SlotsEvent.UseUserPreset(preset.name)) },
                selected = state.activePreset == PresetRef.User(preset.name),
                badge = { CloseButton(onClick = { onEvent(SlotsEvent.RequestDelete(preset.name)) }) },
            )
        }
    }
}

private val BuiltInLayout.title: StringResource
    get() = when (this) {
        BuiltInLayout.TwoRows -> Res.string.slots_preset_two_rows
        BuiltInLayout.Sides -> Res.string.slots_preset_sides
        BuiltInLayout.Arc -> Res.string.slots_preset_arc
        BuiltInLayout.Block -> Res.string.slots_preset_block
    }
