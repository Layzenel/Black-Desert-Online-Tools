package com.layzen.bdotools.feature.slots.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.domain.GamePreset
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.presentation.ui.gamePresetTitle
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_apply_confirm
import com.layzen.bdotools.feature.slots.resources.slots_apply_description
import com.layzen.bdotools.feature.slots.resources.slots_apply_target
import com.layzen.bdotools.feature.slots.resources.slots_apply_title
import com.layzen.bdotools.feature.slots.resources.slots_cancel
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** «Применить в игру»: в какой набор позиций записать и предупреждение, что игра должна быть закрыта. */
@Composable
internal fun ApplyDialog(dialog: SlotsDialog.ConfirmApply, presets: List<GamePreset>, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    ModalWindow(
        title = stringResource(Res.string.slots_apply_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(stringResource(Res.string.slots_apply_confirm), onClick = { onEvent(SlotsEvent.ConfirmApply) })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            SectionHeader(stringResource(Res.string.slots_apply_target))
            OptionGroup(presets, dialog.target, { onEvent(SlotsEvent.SelectApplyTarget(it)) }, { gamePresetTitle(it) })
            WarningText(stringResource(Res.string.slots_apply_description))
        }
    }
}
