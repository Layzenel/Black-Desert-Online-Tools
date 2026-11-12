package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import com.layzen.bdotools.feature.uieditor.domain.preset.BuiltInLayout
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.menu_delete
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_5plus5and5plus5
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_arc
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_game
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_horseshoe
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_key_clusters
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_ring
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_sides
import com.layzen.bdotools.feature.uieditor.resources.slots_preset_wings
import com.layzen.bdotools.feature.uieditor.resources.slots_presets
import com.layzen.bdotools.feature.uieditor.resources.slots_presets_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_user_presets_hint
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_user_preset_delete
import com.layzen.bdotools.feature.uieditor.resources.slots_user_presets
import com.layzen.bdotools.feature.uieditor.resources.slots_user_presets_empty
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Списки «Готовые пресеты» (раскладка из файла игры и готовые раскладки слотов перезарядки) и «Мои пресеты»
 * (сохранённые раскладки, у каждой — корзина удаления). Пока своих пресетов нет — подсказка, где они лежат.
 * При наведении на список — всплывающая подсказка, что в нём.
 */
@Composable
internal fun PresetList(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    BuiltInPresets(onEvent)
    HoverTooltip(stringResource(Res.string.slots_user_presets_hint), Modifier.fillMaxWidth()) {
        DropdownSelect(
            selected = null,
            options = state.presets.map { it.name },
            onSelect = { onEvent(SlotsEvent.UseUserPreset(it)) },
            label = { it },
            modifier = Modifier.fillMaxWidth(),
            fieldText = stringResource(Res.string.slots_user_presets),
            minHeight = SlotsDimens.sidebarSelectHeight,
            trailing = { name -> DeleteButton { onEvent(SlotsEvent.RequestDelete(name)) } },
        )
    }
    if (state.presets.isEmpty()) DescriptionText(stringResource(Res.string.slots_user_presets_empty))
}

/** Корзина у пресета в списке: спрашивает подтверждение и удаляет; при наведении золотая. */
@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val colors = AppTheme.colors
    Image(
        painterResource(Res.drawable.menu_delete),
        contentDescription = stringResource(Res.string.slots_user_preset_delete),
        modifier = Modifier
            .size(AppTheme.dimens.iconSize)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick),
        colorFilter = ColorFilter.tint(if (hovered) colors.accentGold else colors.textPrimary),
    )
}

/** Готовые раскладки — выпадающим списком в стиле поля выбора, чтобы не занимать меню. */
@Composable
private fun BuiltInPresets(onEvent: (SlotsEvent) -> Unit) {
    val options = listOf<PresetRef>(PresetRef.Game) + BuiltInLayout.entries.map { PresetRef.BuiltIn(it) }
    HoverTooltip(stringResource(Res.string.slots_presets_hint), Modifier.fillMaxWidth()) {
        DropdownSelect(
            selected = null,
            options = options,
            onSelect = { ref -> onEvent(if (ref is PresetRef.BuiltIn) SlotsEvent.UseBuiltIn(ref.layout) else SlotsEvent.UseGameLayout) },
            label = { ref -> stringResource(if (ref is PresetRef.BuiltIn) ref.layout.title else Res.string.slots_preset_game) },
            modifier = Modifier.fillMaxWidth(),
            fieldText = stringResource(Res.string.slots_presets),
            minHeight = SlotsDimens.sidebarSelectHeight,
        )
    }
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
