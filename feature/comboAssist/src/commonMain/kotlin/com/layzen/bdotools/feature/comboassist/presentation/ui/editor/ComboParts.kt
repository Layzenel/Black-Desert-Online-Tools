package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_part_normal
import com.layzen.bdotools.feature.comboassist.resources.combo_part_rage
import com.layzen.bdotools.uikit.components.TabBar
import org.jetbrains.compose.resources.stringResource

/**
 * Какую часть комбо править: обычное или под гневом Чёрного духа — вкладки, пока у комбо есть вариант под гневом.
 * Добавить его и скопировать в него обычное комбо — кнопки в панели инструментов, убрать — внизу меню слева.
 */
@Composable
internal fun ComboParts(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    if (state.combo?.rage == null) return
    TabBar(
        listOf(false, true), state.rageEdited, { onEvent(ComboEvent.EditRage(it)) },
        { rage -> stringResource(if (rage) Res.string.combo_part_rage else Res.string.combo_part_normal) },
    )
}
