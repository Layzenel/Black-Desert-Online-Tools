package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.layzen.bdotools.composable.overlay.GameOverlayWindow
import com.layzen.bdotools.composable.overlay.OverlayAnchor
import com.layzen.bdotools.composable.overlay.OverlayContribution
import com.layzen.bdotools.composable.overlay.OverlayTab
import com.layzen.bdotools.composable.overlay.ScaledOverlay
import com.layzen.bdotools.feature.comboassist.presentation.ComboViewModel
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_tab_combo
import com.layzen.bdotools.feature.comboassist.resources.combo_setting_slot_controls
import com.layzen.bdotools.feature.comboassist.resources.combo_setting_slot_controls_description
import com.layzen.bdotools.feature.comboassist.resources.combo_setting_slot_shields
import com.layzen.bdotools.feature.comboassist.resources.combo_setting_slot_shields_description
import com.layzen.bdotools.feature.comboassist.resources.combo_setting_slot_tooltips
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.ToggleSwitch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Маршрут вкладки комбо в программе: «Редактировать комбо» в меню оверлея открывает её. */
private const val COMBO_ROUTE = "combo"

/** Окно схемы комбо: своё место на экране и свой режим перемещения. */
private const val SCHEMA_WINDOW = "combo-schema"

private const val COMBO_ORDER = 10

/**
 * Что помощник комбо добавляет поверх игры: трекер комбо, окно схемы комбо и тултипы слотов перезарядки, а в меню оверлея —
 * вкладка «Комбо» и пункт общей вкладки «Настройки». Окна живут вне окна программы, поэтому ViewModel берётся из владельца приложения,
 * а подписка на состояние — без жизненного цикла окна.
 */
internal fun comboOverlayContribution(): OverlayContribution = OverlayContribution(
    id = COMBO_ROUTE,
    windows = { ComboOverlayWindows(koinViewModel()) },
    settings = { SettingsSection(koinViewModel()) },
    tabs = listOf(
        OverlayTab(COMBO_ORDER, Res.string.combo_panel_tab_combo) { scope ->
            val viewModel: ComboViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()
            // схема комбо под курсором — рядом с меню, чтобы было видно, что это за комбо
            OverlayComboTab(state, viewModel::onEvent, onEdit = scope::openApp, onHover = { combo -> scope.aside(combo?.let { { ComboPreview(it, viewModel) } }) })
            DisposableEffect(Unit) { onDispose { scope.aside(null) } }
        },
    ),
)

@Composable
private fun ComboOverlayWindows(viewModel: ComboViewModel) {
    if (!viewModel.canWatchGame) return
    val state by viewModel.state.collectAsState()
    val hidden by viewModel.hiddenSlots.collectAsState()
    val visibleSlots = state.slots.filterIndexed { index, _ -> index !in hidden }
    // все слоты закрыты окном игры (умения, настройки, карта) — трекер и схема прячутся вместе с ними
    val slotsCovered = state.slots.isNotEmpty() && visibleSlots.isEmpty()
    state.tracked?.let { combo ->
        val settings = state.settings
        if (state.overlayShown && !slotsCovered) {
            GameOverlayWindow(COMBO_ROUTE, OverlayAnchor.BottomCenter, ComboDimens.overlayWindow) {
                ScaledOverlay(settings.comboScale, settings.comboOpacity) {
                    ComboOverlayContent(combo, state.overlay, state.skills, viewModel::details, state.rageTracked)
                }
            }
        }
        if (state.overlayShown && settings.schemaShown && !slotsCovered) {
            val size = if (combo.vertical) ComboDimens.overlayWindowVertical else ComboDimens.overlayWindow
            GameOverlayWindow(SCHEMA_WINDOW, OverlayAnchor.CenterLeft, size) {
                ScaledOverlay(settings.comboScale, settings.comboOpacity) {
                    SchemaOverlay(combo, state.overlay, state.skills, viewModel::details, inGame = true, rage = state.rageTracked)
                }
            }
        }
    }
    val protections = if (state.settings.slotShields) state.slotProtections else emptyMap()
    val controls = if (state.settings.slotControls) state.slotControls else emptyMap()
    if (state.settings.slotTooltips) SlotTooltipWindow(visibleSlots, state.skills, viewModel::details, protections, controls)
    if (state.settings.slotShields || state.settings.slotControls) SlotShieldsWindow(visibleSlots, protections, controls)
}

/** Пункт общей вкладки «Настройки»: тултипы над слотами перезарядки, щиты защиты и значки контролей на слотах. */
@Composable
private fun SettingsSection(viewModel: ComboViewModel) {
    val state by viewModel.state.collectAsState()
    ToggleSwitch(
        state.settings.slotTooltips, { viewModel.onEvent(ComboEvent.ToggleSlotTooltips) },
        label = stringResource(Res.string.combo_setting_slot_tooltips),
    )
    SectionDivider()
    ToggleSwitch(
        state.settings.slotShields, { viewModel.onEvent(ComboEvent.ToggleSlotShields) },
        label = stringResource(Res.string.combo_setting_slot_shields),
        description = stringResource(Res.string.combo_setting_slot_shields_description),
    )
    SectionDivider()
    ToggleSwitch(
        state.settings.slotControls, { viewModel.onEvent(ComboEvent.ToggleSlotControls) },
        label = stringResource(Res.string.combo_setting_slot_controls),
        description = stringResource(Res.string.combo_setting_slot_controls_description),
    )
}
