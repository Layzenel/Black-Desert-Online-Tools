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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
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
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_move
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_vertical
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_style_next
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_style_schema
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_position
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_scale
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_opacity
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_style
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
 * в редакторе ([onEdit]), вид оверлея, вертикальный оверлей, режим перемещения, размер и непрозрачность.
 */
@Composable
internal fun OverlayComboTab(state: ComboState, onEvent: (ComboEvent) -> Unit, onEdit: () -> Unit) {
    SectionHeader(stringResource(Res.string.combo_menu_show))
    ComboList(state, onEvent)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        if (state.overlayShown) ActionButton(stringResource(Res.string.combo_menu_hide), { onEvent(ComboEvent.HideCombo) })
        ActionButton(stringResource(Res.string.combo_menu_edit), { onEvent(ComboEvent.SetMode(ComboMode.Editor)); onEdit() })
    }
    state.combo?.let { combo ->
        SectionHeader(stringResource(Res.string.combo_panel_style))
        OptionGroup(OverlayStyle.entries, combo.overlayStyle, { onEvent(ComboEvent.SetOverlayStyle(it)) }, {
            stringResource(if (it == OverlayStyle.Schema) Res.string.combo_overlay_style_schema else Res.string.combo_overlay_style_next)
        })
    }
    SectionHeader(stringResource(Res.string.combo_panel_position))
    state.combo?.let { combo ->
        ToggleSwitch(combo.vertical, { onEvent(ComboEvent.ToggleVertical) }, label = stringResource(Res.string.combo_overlay_vertical))
    }
    ToggleSwitch(state.overlayMovable, { onEvent(ComboEvent.ToggleOverlayMove) }, label = stringResource(Res.string.combo_overlay_move))
    val settings = state.settings
    SectionHeader(stringResource(Res.string.combo_panel_scale, settings.comboScale))
    ValueSlider(
        settings.comboScale, { onEvent(ComboEvent.SetComboScale(it, save = false)) }, COMBO_SCALE, Modifier.fillMaxWidth(),
        onValueChangeFinished = { onEvent(ComboEvent.SetComboScale(settings.comboScale, save = true)) },
    )
    SectionHeader(stringResource(Res.string.combo_panel_opacity, settings.comboOpacity))
    ValueSlider(
        settings.comboOpacity, { onEvent(ComboEvent.SetComboOpacity(it, save = false)) }, COMBO_OPACITY, Modifier.fillMaxWidth(),
        onValueChangeFinished = { onEvent(ComboEvent.SetComboOpacity(settings.comboOpacity, save = true)) },
    )
}

/**
 * Сохранённые комбо всех классов с иконкой и названием класса справа; открытое в оверлее подсвечено. Комбо не того
 * класса, которым играют ([ComboState.playedClass]), бледные и не выбираются. Список своей высоты.
 */
@Composable
private fun ComboList(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    if (state.combos.isEmpty()) {
        DescriptionText(stringResource(Res.string.combo_menu_empty))
        return
    }
    val untitled = stringResource(Res.string.combo_untitled)
    val tint = ColorFilter.tint(AppTheme.colors.textBeige)
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().heightIn(max = ComboDimens.overlayPanelListHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
        state.combos.forEach { combo ->
            val characterClass = CharacterClass.entries.firstOrNull { it.key == combo.classKey }
            // комбо другого класса персонажу в игре не подходит: видно, но не выбирается
            val fits = state.playedClass == null || combo.classKey == state.playedClass
            MenuItem(
                combo.name.ifBlank { untitled }, { if (fits) onEvent(ComboEvent.ShowCombo(combo.id)) },
                Modifier.fillMaxWidth().alpha(if (fits) 1f else UNFIT_ALPHA),
                hotkey = characterClass?.let { stringResource(it.title) },
                badge = characterClass?.let { { Image(painterResource(it.icon), null, Modifier.size(ComboDimens.paletteIcon), colorFilter = tint) } },
                selected = state.overlayShown && state.combo?.id == combo.id,
            )
        }
    }
}

/** Размер оверлея комбо, проценты. */
private val COMBO_SCALE = 50..200

/** Непрозрачность оверлея комбо, проценты: совсем прозрачный не нужен — его не найти. */
private val COMBO_OPACITY = 20..100

/** Комбо другого класса — бледное. */
private const val UNFIT_ALPHA = 0.4f
