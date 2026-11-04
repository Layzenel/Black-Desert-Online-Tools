package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_apply
import com.layzen.bdotools.feature.slots.resources.slots_export
import com.layzen.bdotools.feature.slots.resources.slots_import
import com.layzen.bdotools.feature.slots.resources.slots_save_as
import com.layzen.bdotools.uikit.components.ActionButton
import org.jetbrains.compose.resources.stringResource

/** Футер окна: файлы раскладки слева, сохранение и главное действие «Применить в игру» справа. */
@Composable
internal fun RowScope.SlotsFooter(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    ActionButton(stringResource(Res.string.slots_import), onClick = { onEvent(SlotsEvent.OpenImport) })
    ActionButton(stringResource(Res.string.slots_export), onClick = { onEvent(SlotsEvent.Export) })
    Spacer(Modifier.weight(1f))
    ActionButton(stringResource(Res.string.slots_save_as), onClick = { onEvent(SlotsEvent.OpenSaveAs) })
    ActionButton(
        stringResource(Res.string.slots_apply),
        onClick = { onEvent(SlotsEvent.OpenApply) },
        enabled = state.hasChanges && !state.busy,
    )
}
