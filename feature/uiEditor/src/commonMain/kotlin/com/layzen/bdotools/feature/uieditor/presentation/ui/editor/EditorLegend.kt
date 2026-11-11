package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_chat
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_class_resource
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_cooldown
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_fixed
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_hotbar
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_interface
import com.layzen.bdotools.feature.uieditor.resources.slots_legend_off_screen
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** В окне «Справка» ([HelpDialog]), столбиком: какой цвет рамки у какой группы элементов и что значит красная заливка. */
@Composable
internal fun EditorLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        LegendItem(SlotGroup.Cooldown.frameColor(), stringResource(Res.string.slots_legend_cooldown))
        LegendItem(SlotGroup.Hotbar.frameColor(), stringResource(Res.string.slots_legend_hotbar))
        LegendItem(SlotGroup.ClassResource.frameColor(), stringResource(Res.string.slots_legend_class_resource))
        LegendItem(SlotGroup.Interface.frameColor(), stringResource(Res.string.slots_legend_interface))
        LegendItem(AppTheme.colors.textBeige, stringResource(Res.string.slots_legend_chat))
        LegendItem(fixedElementColor(), stringResource(Res.string.slots_legend_fixed), fill = fixedElementFill())
        // За краем экрана элемент залит красным, а рамка остаётся цветом своей группы — поэтому в легенде только заливка
        val offScreen = AppTheme.colors.danger.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA)
        LegendItem(offScreen, stringResource(Res.string.slots_legend_off_screen), fill = offScreen)
    }
}

@Composable
private fun LegendItem(color: Color, text: String, fill: Color = Color.Transparent) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Box(Modifier.size(SlotsDimens.legendSwatch).background(fill).border(SlotsDimens.slotBorder, color))
        Text(text, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
    }
}
