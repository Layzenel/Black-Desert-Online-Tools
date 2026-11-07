package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.gamePresetTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_game_preset
import com.layzen.bdotools.uikit.components.DropdownSelect
import org.jetbrains.compose.resources.stringResource

/**
 * «Пресеты в игре» — список наборов позиций из файла игры: общий или «Пресет 1–3».
 * Пресеты, которых нет в файле, не показываются.
 */
@Composable
internal fun GamePresetSettings(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val loaded = state.loaded ?: return
    DropdownSelect(
        selected = null,
        options = loaded.presets.keys.sorted(),
        onSelect = { onEvent(SlotsEvent.SelectGamePreset(it)) },
        label = { gamePresetTitle(it) },
        modifier = Modifier.fillMaxWidth(),
        fieldText = stringResource(Res.string.slots_game_preset),
    )
}
