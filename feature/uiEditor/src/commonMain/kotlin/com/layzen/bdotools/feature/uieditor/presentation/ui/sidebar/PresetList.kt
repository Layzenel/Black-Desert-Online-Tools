package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.BuiltInLayout
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_arc
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_5plus5and5plus5
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_game
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_horseshoe
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_key_clusters
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_ring
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_sides
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_wings
import com.layzen.bdotools.feature.uieditor.resources.slots_presets
import com.layzen.bdotools.feature.uieditor.resources.slots_user_presets
import com.layzen.bdotools.feature.uieditor.resources.slots_user_presets_empty
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Кнопка «Готовые пресеты» со списком: раскладка из файла игры и готовые раскладки слотов перезарядки.
 * Ниже — пресеты пользователя.
 */
@Composable
internal fun PresetList(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    BuiltInPresets(state.activePreset, onEvent)
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

/** Готовые раскладки — выпадающим списком в стиле поля выбора, чтобы не занимать меню; выбранная подсвечена. */
@Composable
private fun BuiltInPresets(active: PresetRef?, onEvent: (SlotsEvent) -> Unit) {
    val options = listOf<PresetRef>(PresetRef.Game) + BuiltInLayout.entries.map { PresetRef.BuiltIn(it) }
    DropdownSelect(
        selected = active?.takeIf { it in options },
        options = options,
        onSelect = { ref -> onEvent(if (ref is PresetRef.BuiltIn) SlotsEvent.UseBuiltIn(ref.layout) else SlotsEvent.UseGameLayout) },
        label = { ref -> stringResource(if (ref is PresetRef.BuiltIn) ref.layout.title else Res.string.slots_preset_game) },
        modifier = Modifier.fillMaxWidth(),
        fieldText = stringResource(Res.string.slots_presets),
    )
}

private val BuiltInLayout.title: StringResource
    get() = when (this) {
        BuiltInLayout.Sides -> Res.string.slots_preset_sides
        BuiltInLayout.Arc -> Res.string.slots_preset_arc
        BuiltInLayout.Horseshoe -> Res.string.slots_preset_horseshoe
        BuiltInLayout.Ring -> Res.string.slots_preset_ring
        BuiltInLayout.Wings -> Res.string.slots_preset_wings
        BuiltInLayout.FivePlusFiveAndFivePlusFive -> Res.string.slots_preset_5plus5and5plus5
        BuiltInLayout.KeyClusters -> Res.string.slots_preset_key_clusters
    }
