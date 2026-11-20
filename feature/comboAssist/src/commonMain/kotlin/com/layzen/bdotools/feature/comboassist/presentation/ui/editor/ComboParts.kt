package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_part_normal
import com.layzen.bdotools.feature.comboassist.resources.combo_part_rage
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_add
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_remove
import com.layzen.bdotools.feature.comboassist.resources.menu_add
import com.layzen.bdotools.feature.comboassist.resources.menu_delete
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Какую часть комбо править: обычное или под гневом Чёрного духа. Если комбо под гневом нет — кнопка «добавить»,
 * пока правят комбо под гневом — ещё и «убрать».
 */
@Composable
internal fun ComboParts(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    val combo = state.combo ?: return
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
        if (combo.rage == null) {
            MenuAction(stringResource(Res.string.combo_rage_add), { onEvent(ComboEvent.AddRage) }, icon = painterResource(Res.drawable.menu_add))
            return@Row
        }
        OptionGroup(
            listOf(false, true), state.rageEdited, { onEvent(ComboEvent.EditRage(it)) },
            { rage -> stringResource(if (rage) Res.string.combo_part_rage else Res.string.combo_part_normal) },
        )
        if (state.rageEdited) {
            MenuAction(stringResource(Res.string.combo_rage_remove), { onEvent(ComboEvent.RemoveRage) }, icon = painterResource(Res.drawable.menu_delete))
        }
    }
}
