package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import com.layzen.bdotools.composable.overlay.OverlayScalePercent
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboMode
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_menu_edit
import com.layzen.bdotools.feature.comboassist.resources.combo_menu_empty
import com.layzen.bdotools.feature.comboassist.resources.combo_menu_hide
import com.layzen.bdotools.feature.comboassist.resources.combo_menu_show
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_vertical
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_scale
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_schema
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_tracker
import com.layzen.bdotools.feature.comboassist.resources.combo_schema_show
import com.layzen.bdotools.feature.comboassist.resources.combo_tracker_limits
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_opacity
import com.layzen.bdotools.feature.comboassist.resources.combo_untitled
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.icons.title
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Вкладка «Комбо» меню оверлея: какое комбо показать (выбор сразу включает его в оверлее), скрыть или открыть
 * в редакторе ([onEdit]); комбо под курсором — в [onHover] (`null` — ни одно); трекер — вид и перемещение; схема — показ, перемещение и ориентация; общие размер
 * и непрозрачность.
 */
@Composable
internal fun OverlayComboTab(state: ComboState, onEvent: (ComboEvent) -> Unit, onEdit: () -> Unit, onHover: (Combo?) -> Unit = {}) {
    SectionHeader(stringResource(Res.string.combo_menu_show))
    ComboList(state, onEvent, onHover)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        if (state.overlayShown) ActionButton(stringResource(Res.string.combo_menu_hide), { onEvent(ComboEvent.HideCombo) })
        ActionButton(stringResource(Res.string.combo_menu_edit), { onEvent(ComboEvent.SetMode(ComboMode.Editor)); onEdit() })
    }
    SectionHeader(stringResource(Res.string.combo_panel_tracker))
    state.combo?.let { combo ->
        OptionGroup(OverlayStyle.entries, combo.overlayStyle, { onEvent(ComboEvent.SetOverlayStyle(it)) }, { stringResource(it.title) }, Modifier.fillMaxWidth())
    }
    DescriptionText(stringResource(Res.string.combo_tracker_limits))
    SectionHeader(stringResource(Res.string.combo_panel_schema))
    ToggleSwitch(state.settings.schemaShown, { onEvent(ComboEvent.ToggleSchema) }, label = stringResource(Res.string.combo_schema_show))
    state.combo?.let { combo ->
        ToggleSwitch(combo.vertical, { onEvent(ComboEvent.ToggleVertical) }, label = stringResource(Res.string.combo_overlay_vertical))
    }
    val settings = state.settings
    val scale = settings.comboScale.coerceIn(OverlayScalePercent)
    SectionHeader(stringResource(Res.string.combo_panel_scale, scale))
    ValueSlider(
        scale, { onEvent(ComboEvent.SetComboScale(it, save = false)) }, OverlayScalePercent, Modifier.fillMaxWidth(),
        onValueChangeFinished = { onEvent(ComboEvent.SetComboScale(scale, save = true)) },
    )
    SectionHeader(stringResource(Res.string.combo_panel_opacity, settings.comboOpacity))
    ValueSlider(
        settings.comboOpacity, { onEvent(ComboEvent.SetComboOpacity(it, save = false)) }, COMBO_OPACITY, Modifier.fillMaxWidth(),
        onValueChangeFinished = { onEvent(ComboEvent.SetComboOpacity(settings.comboOpacity, save = true)) },
    )
    ResetSecondsSlider(settings.resetSeconds, onEvent)
}

/**
 * Сохранённые комбо всех классов с иконкой и названием класса справа; открытое в оверлее подсвечено. Комбо не того
 * класса, которым играют ([ComboState.playedClass]), бледные и не выбираются. Список своей высоты.
 */
@Composable
private fun ComboList(state: ComboState, onEvent: (ComboEvent) -> Unit, onHover: (Combo?) -> Unit) {
    if (state.combos.isEmpty()) {
        DescriptionText(stringResource(Res.string.combo_menu_empty))
        return
    }
    val untitled = stringResource(Res.string.combo_untitled)
    val tint = ColorFilter.tint(AppTheme.colors.textBeige)
    val scroll = rememberScrollState()
    var hovered by remember { mutableStateOf<Combo?>(null) }
    LaunchedEffect(hovered) { onHover(hovered) }
    Column(Modifier.fillMaxWidth().heightIn(max = ComboDimens.overlayPanelListHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
        state.combos.forEach { combo ->
            val characterClass = CharacterClass.entries.firstOrNull { it.key == combo.classKey }
            // комбо другого класса персонажу в игре не подходит: видно, но не выбирается
            val fits = state.playedClass == null || combo.classKey == state.playedClass
            val hover = remember { MutableInteractionSource() }
            val over by hover.collectIsHoveredAsState()
            LaunchedEffect(over) { if (over) hovered = combo else if (hovered?.id == combo.id) hovered = null }
            MenuItem(
                combo.name.ifBlank { untitled }, { if (fits) onEvent(ComboEvent.ShowCombo(combo.id)) },
                Modifier.fillMaxWidth().hoverable(hover).alpha(if (fits) 1f else UNFIT_ALPHA),
                hotkey = characterClass?.let { stringResource(it.title) },
                badge = characterClass?.let { { Image(painterResource(it.icon), null, Modifier.size(ComboDimens.paletteIcon), colorFilter = tint) } },
                selected = state.overlayShown && state.combo?.id == combo.id,
            )
        }
    }
}

/** Непрозрачность оверлея комбо, проценты: совсем прозрачный не нужен — его не найти. */
private val COMBO_OPACITY = 20..100

/** Комбо другого класса — бледное. */
private const val UNFIT_ALPHA = 0.4f
