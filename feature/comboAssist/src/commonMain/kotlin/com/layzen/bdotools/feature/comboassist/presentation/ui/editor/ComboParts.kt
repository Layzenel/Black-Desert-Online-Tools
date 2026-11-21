package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_part_normal
import com.layzen.bdotools.feature.comboassist.resources.combo_part_rage
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_add
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_copy
import com.layzen.bdotools.feature.comboassist.resources.menu_copy
import com.layzen.bdotools.feature.comboassist.resources.menu_add
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Какую часть комбо править: обычное или под гневом Чёрного духа. Если комбо под гневом нет — кнопка «добавить»,
 * иначе переключатель, а на вкладке гнева — копия обычного комбо; убрать комбо под гневом — внизу меню слева.
 */
@Composable
internal fun ComboParts(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    val combo = state.combo ?: return
    // переключатель частей во всю ширину: кнопка копии — под ним, иначе её не видно
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        if (combo.rage == null) {
            MenuAction(stringResource(Res.string.combo_rage_add), { onEvent(ComboEvent.AddRage) }, icon = painterResource(Res.drawable.menu_add))
            return@Column
        }
        OptionGroup(
            listOf(false, true), state.rageEdited, { onEvent(ComboEvent.EditRage(it)) },
            { rage -> stringResource(if (rage) Res.string.combo_part_rage else Res.string.combo_part_normal) },
        )
        // комбо под гневом обычно — то же комбо с парой замен: копия обычного, а дальше правятся отличия
        if (state.rageEdited) {
            MenuAction(stringResource(Res.string.combo_rage_copy), { onEvent(ComboEvent.CopyToRage) }, icon = painterResource(Res.drawable.menu_copy))
        }
    }
}
