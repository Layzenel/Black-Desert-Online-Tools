package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.menu_apply
import com.layzen.bdotools.feature.slots.resources.menu_export
import com.layzen.bdotools.feature.slots.resources.menu_import
import com.layzen.bdotools.feature.slots.resources.menu_reload
import com.layzen.bdotools.feature.slots.resources.menu_save
import com.layzen.bdotools.feature.slots.resources.slots_apply
import com.layzen.bdotools.feature.slots.resources.slots_export
import com.layzen.bdotools.feature.slots.resources.slots_import
import com.layzen.bdotools.feature.slots.resources.slots_reload
import com.layzen.bdotools.feature.slots.resources.slots_save_as
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Низ меню слева, всегда на виду: перечитать файл игры, импорт и экспорт раскладки, сохранение пресета
 * и последним — главное действие «Применить в игру».
 */
@Composable
internal fun SlotsFileActions(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val wide = Modifier.fillMaxWidth()
    MenuAction(
        stringResource(Res.string.slots_reload),
        onClick = { onEvent(SlotsEvent.Reload) },
        modifier = wide,
        icon = painterResource(Res.drawable.menu_reload),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        MenuAction(
            stringResource(Res.string.slots_import),
            onClick = { onEvent(SlotsEvent.OpenImport) },
            modifier = Modifier.weight(1f),
            icon = painterResource(Res.drawable.menu_import),
        )
        MenuAction(
            stringResource(Res.string.slots_export),
            onClick = { onEvent(SlotsEvent.Export) },
            modifier = Modifier.weight(1f),
            icon = painterResource(Res.drawable.menu_export),
        )
    }
    MenuAction(
        stringResource(Res.string.slots_save_as),
        onClick = { onEvent(SlotsEvent.OpenSaveAs) },
        modifier = wide,
        icon = painterResource(Res.drawable.menu_save),
    )
    MenuAction(
        stringResource(Res.string.slots_apply),
        onClick = { onEvent(SlotsEvent.OpenApply) },
        modifier = wide,
        icon = painterResource(Res.drawable.menu_apply),
        enabled = state.hasChanges && !state.busy,
    )
}
