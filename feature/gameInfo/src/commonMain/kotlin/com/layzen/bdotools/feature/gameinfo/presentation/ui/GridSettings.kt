package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep
import com.layzen.bdotools.feature.gameinfo.presentation.GameInfoViewModel
import com.layzen.bdotools.feature.gameinfo.presentation.event.GameInfoEvent
import com.layzen.bdotools.feature.gameinfo.resources.Res
import com.layzen.bdotools.feature.gameinfo.resources.game_info_grid_opacity
import com.layzen.bdotools.feature.gameinfo.resources.game_info_grid_show
import com.layzen.bdotools.feature.gameinfo.resources.game_info_grid_step
import com.layzen.bdotools.feature.gameinfo.resources.game_info_grid_unreadable
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.components.WarningText
import org.jetbrains.compose.resources.stringResource

/** Пункты вкладки «Настройки»: показ сетки на весь экран игры, её непрозрачность и шаг (видны, пока сетка включена). */
@Composable
internal fun GridSettings(viewModel: GameInfoViewModel) {
    val state by viewModel.state.collectAsState()
    ToggleSwitch(state.settings.gridShown, { viewModel.onEvent(GameInfoEvent.ToggleGrid) }, label = stringResource(Res.string.game_info_grid_show))
    if (!state.settings.gridShown) return
    val opacity = state.settings.gridOpacity
    SectionHeader(stringResource(Res.string.game_info_grid_opacity, opacity))
    ValueSlider(
        opacity, { viewModel.onEvent(GameInfoEvent.SetGridOpacity(it, save = false)) }, OPACITY, Modifier.fillMaxWidth(),
        onValueChangeFinished = { viewModel.onEvent(GameInfoEvent.SetGridOpacity(opacity, save = true)) },
    )
    SectionHeader(stringResource(Res.string.game_info_grid_step))
    OptionGroup(
        GridStep.entries, state.settings.gridStep, { viewModel.onEvent(GameInfoEvent.SetGridStep(it)) }, { it.size.toString() },
        Modifier.fillMaxWidth(),
    )
    if (state.gridUnreadable) WarningText(stringResource(Res.string.game_info_grid_unreadable))
}
