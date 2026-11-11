package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_split_hp
import com.layzen.bdotools.feature.uieditor.resources.slots_split_hp_description
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.ToggleSwitch
import org.jetbrains.compose.resources.stringResource

/**
 * «Разделение HP» игры: на холсте здоровье, PVP и гнев Чёрного духа отдельно или одна неподвижная панель слева вверху.
 * Стоит в меню редактора и в превью импорта и экспорта. `null` в [splitHp] — как включённое.
 */
@Composable
internal fun SplitHpSettings(splitHp: Boolean?, onChange: (Boolean) -> Unit) {
    HoverTooltip(stringResource(Res.string.slots_split_hp_description)) {
        ToggleSwitch(
            checked = splitHp != false,
            onCheckedChange = onChange,
            label = stringResource(Res.string.slots_split_hp),
        )
    }
}
