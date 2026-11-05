package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_redo
import com.layzen.bdotools.feature.slots.resources.slots_undo
import com.layzen.bdotools.uikit.components.ActionBar
import com.layzen.bdotools.uikit.components.ActionButton
import org.jetbrains.compose.resources.stringResource

/** Панель над холстом: отмена и повтор правок. Работа с файлами — внизу меню слева. */
@Composable
internal fun EditorActionBar(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    ActionBar {
        ActionButton(stringResource(Res.string.slots_undo), onClick = { onEvent(SlotsEvent.Undo) }, enabled = state.history.isNotEmpty())
        ActionButton(stringResource(Res.string.slots_redo), onClick = { onEvent(SlotsEvent.Redo) }, enabled = state.future.isNotEmpty())
    }
}
