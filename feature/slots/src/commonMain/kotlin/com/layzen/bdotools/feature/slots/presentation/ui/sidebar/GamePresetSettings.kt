package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.gamePresetTitle
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_game_preset
import com.layzen.bdotools.feature.slots.resources.slots_game_preset_description
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Какой набор позиций из файла игры открыт в редакторе: общий или «Пресет 1–3». Пресеты, которых нет в файле, не показываются. */
@Composable
internal fun GamePresetSettings(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val loaded = state.loaded ?: return
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(stringResource(Res.string.slots_game_preset), description = stringResource(Res.string.slots_game_preset_description))
        DropdownSelect(
            selected = loaded.edited,
            options = loaded.presets.keys.sorted(),
            onSelect = { onEvent(SlotsEvent.SelectGamePreset(it)) },
            label = { gamePresetTitle(it) },
        )
    }
}
