package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.composable.overlay.GameOverlayWindow
import com.layzen.bdotools.composable.overlay.OverlayAnchor
import com.layzen.bdotools.composable.overlay.OverlayContribution
import com.layzen.bdotools.composable.overlay.OverlayScalePercent
import com.layzen.bdotools.composable.overlay.OverlayTab
import com.layzen.bdotools.composable.overlay.ScaledOverlay
import com.layzen.bdotools.feature.gameinfo.presentation.GameInfoViewModel
import com.layzen.bdotools.feature.gameinfo.presentation.event.GameInfoEvent
import com.layzen.bdotools.feature.gameinfo.resources.Res
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_needs_admin
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_opacity
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_scale
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_show
import com.layzen.bdotools.feature.gameinfo.resources.game_info_tab
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.components.WarningText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Окно пинга запоминает место под этим именем. */
private const val PING_WINDOW = "ping"

/** У фичи нет своей вкладки в программе: «открыть программу» ей не нужно. */
private const val GAME_INFO_ID = "game-info"

private const val INFO_ORDER = 20

/** Что фича добавляет поверх игры: окно пинга и вкладку «Информация» в меню оверлея. */
internal fun gameInfoOverlayContribution(): OverlayContribution = OverlayContribution(
    id = GAME_INFO_ID,
    windows = { PingWindow(koinViewModel()) },
    tabs = listOf(OverlayTab(INFO_ORDER, Res.string.game_info_tab) { InfoTab(koinViewModel()) }),
)

@Composable
private fun PingWindow(viewModel: GameInfoViewModel) {
    val state by viewModel.state.collectAsState()
    if (!viewModel.supported || !state.settings.shown) return
    GameOverlayWindow(PING_WINDOW, OverlayAnchor.TopRight, GameInfoDimens.pingWindow) {
        ScaledOverlay(state.settings.scale, state.settings.opacity) { PingBadge(state.ping) }
    }
}

/** Вкладка «Информация»: пинг игры — показ, размер и непрозрачность. */
@Composable
private fun InfoTab(viewModel: GameInfoViewModel) {
    val state by viewModel.state.collectAsState()
    val settings = state.settings
    val onEvent = viewModel::onEvent
    SectionHeader(stringResource(Res.string.game_info_ping))
    ToggleSwitch(settings.shown, { onEvent(GameInfoEvent.TogglePing) }, label = stringResource(Res.string.game_info_ping_show))
    // без прав администратора задержку нового подключения игры не собрать: пинг пустой, а почему — здесь
    if (settings.shown && state.needsAdmin) WarningText(stringResource(Res.string.game_info_ping_needs_admin))
    if (!settings.shown) return
    val scale = settings.scale.coerceIn(OverlayScalePercent)
    SectionHeader(stringResource(Res.string.game_info_ping_scale, scale))
    ValueSlider(
        scale, { onEvent(GameInfoEvent.SetScale(it, save = false)) }, OverlayScalePercent, Modifier.fillMaxWidth(),
        onValueChangeFinished = { onEvent(GameInfoEvent.SetScale(scale, save = true)) },
    )
    SectionHeader(stringResource(Res.string.game_info_ping_opacity, settings.opacity))
    ValueSlider(
        settings.opacity, { onEvent(GameInfoEvent.SetOpacity(it, save = false)) }, OPACITY, Modifier.fillMaxWidth(),
        onValueChangeFinished = { onEvent(GameInfoEvent.SetOpacity(settings.opacity, save = true)) },
    )
}

/** Непрозрачность пинга, проценты. */
private val OPACITY = 20..100
