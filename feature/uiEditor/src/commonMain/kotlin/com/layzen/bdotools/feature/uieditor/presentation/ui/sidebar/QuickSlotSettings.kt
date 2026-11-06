package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_custom_quick_slots
import com.layzen.bdotools.feature.uieditor.resources.slots_custom_quick_slots_description
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.ToggleSwitch
import org.jetbrains.compose.resources.stringResource

/**
 * «Настройка быстрых слотов» игры: на холсте 20 слотов хотбара или одна панель «Быстрый слот».
 * Стоит в меню редактора и в превью импорта и экспорта. `null` в [customQuickSlots] — как включённая.
 */
@Composable
internal fun QuickSlotSettings(customQuickSlots: Boolean?, onChange: (Boolean) -> Unit) {
    HoverTooltip(stringResource(Res.string.slots_custom_quick_slots_description)) {
        ToggleSwitch(
            checked = customQuickSlots != false,
            onCheckedChange = onChange,
            label = stringResource(Res.string.slots_custom_quick_slots),
        )
    }
}
