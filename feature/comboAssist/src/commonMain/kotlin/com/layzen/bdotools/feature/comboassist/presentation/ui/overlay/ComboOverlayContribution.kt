package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.layzen.bdotools.composable.overlay.GameOverlayWindow
import com.layzen.bdotools.composable.overlay.OverlayAnchor
import com.layzen.bdotools.composable.overlay.OverlayContribution
import com.layzen.bdotools.composable.overlay.OverlayTab
import com.layzen.bdotools.composable.overlay.ScaledOverlay
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.presentation.ComboViewModel
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_tab_combo
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_tab_settings
import com.layzen.bdotools.feature.comboassist.resources.combo_setting_slot_tooltips
import com.layzen.bdotools.uikit.components.ToggleSwitch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Маршрут вкладки комбо в программе: «Редактировать комбо» в меню оверлея открывает её. */
private const val COMBO_ROUTE = "combo"

private const val SETTINGS_ORDER = 0
private const val COMBO_ORDER = 10

/**
 * Что помощник комбо добавляет поверх игры: оверлей комбо и тултипы слотов перезарядки, а в меню оверлея —
 * вкладки «Настройки» и «Комбо». Окна живут вне окна программы, поэтому ViewModel берётся из владельца приложения,
 * а подписка на состояние — без жизненного цикла окна.
 */
internal fun comboOverlayContribution(): OverlayContribution = OverlayContribution(
    id = COMBO_ROUTE,
    windows = { ComboOverlayWindows(koinViewModel()) },
    tabs = listOf(
        OverlayTab(SETTINGS_ORDER, Res.string.combo_panel_tab_settings) { SettingsTab(koinViewModel()) },
        OverlayTab(COMBO_ORDER, Res.string.combo_panel_tab_combo) { scope ->
            val viewModel: ComboViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()
            OverlayComboTab(state, viewModel::onEvent, onEdit = scope::openApp)
        },
    ),
)

@Composable
private fun ComboOverlayWindows(viewModel: ComboViewModel) {
    if (!viewModel.canWatchGame) return
    val state by viewModel.state.collectAsState()
    state.combo?.let { combo ->
        if (state.overlayShown || state.overlayMovable) {
            val size = if (combo.vertical) ComboDimens.overlayWindowVertical else ComboDimens.overlayWindow
            GameOverlayWindow(COMBO_ROUTE, OverlayAnchor.BottomCenter, size, state.overlayMovable) {
                ScaledOverlay(state.settings.comboScale, state.settings.comboOpacity) {
                    when (combo.overlayStyle) {
                        OverlayStyle.Next -> NextSkillsOverlay(state.overlay, state.skills, viewModel::details, combo.vertical)
                        OverlayStyle.Schema -> ComboOverlay(combo, state.overlay, state.skills, viewModel::details, inGame = true)
                    }
                }
            }
        }
    }
    if (state.settings.slotTooltips) SlotTooltipWindow(state.slots, state.skills, viewModel::details)
}

/** Вкладка «Настройки»: тултипы над слотами перезарядки. */
@Composable
private fun SettingsTab(viewModel: ComboViewModel) {
    val state by viewModel.state.collectAsState()
    ToggleSwitch(
        state.settings.slotTooltips, { viewModel.onEvent(ComboEvent.ToggleSlotTooltips) },
        label = stringResource(Res.string.combo_setting_slot_tooltips),
    )
}
