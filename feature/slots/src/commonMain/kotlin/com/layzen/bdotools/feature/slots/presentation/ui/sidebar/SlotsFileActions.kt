package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_apply
import com.layzen.bdotools.feature.slots.resources.slots_export
import com.layzen.bdotools.feature.slots.resources.slots_import
import com.layzen.bdotools.feature.slots.resources.slots_reload
import com.layzen.bdotools.feature.slots.resources.slots_save_as
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Низ меню слева, всегда на виду: перечитать файл игры, импорт и экспорт раскладки, сохранение пресета
 * и последним — главное действие «Применить в игру».
 */
@Composable
internal fun SlotsFileActions(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val wide = Modifier.fillMaxWidth()
    ActionButton(stringResource(Res.string.slots_reload), onClick = { onEvent(SlotsEvent.Reload) }, modifier = wide)
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        ActionButton(stringResource(Res.string.slots_import), onClick = { onEvent(SlotsEvent.OpenImport) }, modifier = Modifier.weight(1f))
        ActionButton(stringResource(Res.string.slots_export), onClick = { onEvent(SlotsEvent.Export) }, modifier = Modifier.weight(1f))
    }
    ActionButton(stringResource(Res.string.slots_save_as), onClick = { onEvent(SlotsEvent.OpenSaveAs) }, modifier = wide)
    ActionButton(
        stringResource(Res.string.slots_apply),
        onClick = { onEvent(SlotsEvent.OpenApply) },
        modifier = wide,
        enabled = state.hasChanges && !state.busy,
    )
}
