package com.layzen.bdotools.feature.comboassist.presentation.ui.overlaysettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.composable.overlay.ScaledOverlay
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.ComboOverlayContent
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.SchemaOverlay
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.title
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_horizontal
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_vertical
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_schema
import com.layzen.bdotools.feature.comboassist.resources.combo_panel_tracker
import com.layzen.bdotools.feature.comboassist.resources.combo_tracker_limits
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Настройка оверлея (только настольная версия): вид трекера и его предпросмотр, под ним — схема комбо и её
 * ориентация. Всё того же размера и непрозрачности, что поверх игры; умения в схеме жмутся кликом по иконке.
 */
@Composable
internal fun OverlaySettingsPanel(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.combo ?: return
    val settings = state.settings
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        SectionHeader(stringResource(Res.string.combo_panel_tracker), description = stringResource(Res.string.combo_tracker_limits))
        OptionGroup(OverlayStyle.entries, combo.overlayStyle, { onEvent(ComboEvent.SetOverlayStyle(it)) }, { stringResource(it.title) },
            Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth))
        ScaledOverlay(settings.comboScale, settings.comboOpacity) { ComboOverlayContent(combo, state.overlay, state.skills, loadDetails) }
        SectionHeader(stringResource(Res.string.combo_panel_schema))
        ScaledOverlay(settings.comboScale, settings.comboOpacity) {
            SchemaOverlay(combo, state.overlay, state.skills, loadDetails, onPress = { onEvent(ComboEvent.Press(it)) })
        }
        ActionButton(stringResource(if (combo.vertical) Res.string.combo_overlay_horizontal else Res.string.combo_overlay_vertical), { onEvent(ComboEvent.ToggleVertical) })
    }
}
