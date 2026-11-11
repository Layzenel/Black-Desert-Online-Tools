package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_save
import com.layzen.bdotools.feature.uieditor.resources.slots_save_as_description
import com.layzen.bdotools.feature.uieditor.resources.slots_save_as_name
import com.layzen.bdotools.feature.uieditor.resources.slots_save_as_replace
import com.layzen.bdotools.feature.uieditor.resources.slots_save_as_title
import com.layzen.bdotools.feature.uieditor.resources.slots_save_replace
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Сохранение раскладки пресетом: новое название или замена своего пресета [presets] из списка — выбор подставляет
 * его название. Если название совпадает с существующим, кнопка — «Заменить».
 */
@Composable
internal fun SaveAsDialog(dialog: SlotsDialog.SaveAs, presets: List<String>, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    val replacing = dialog.name.trim() in presets
    ModalWindow(
        title = stringResource(Res.string.slots_save_as_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(
                stringResource(if (replacing) Res.string.slots_save_replace else Res.string.slots_save),
                onClick = { onEvent(SlotsEvent.ConfirmSaveAs) },
                enabled = dialog.name.isNotBlank(),
            )
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            InputField(
                value = dialog.name,
                onValueChange = { onEvent(SlotsEvent.SaveAsNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.slots_save_as_name),
            )
            if (presets.isNotEmpty()) {
                DropdownSelect(
                    selected = null,
                    options = presets,
                    onSelect = { onEvent(SlotsEvent.SaveAsNameChanged(it)) },
                    label = { it },
                    modifier = Modifier.fillMaxWidth(),
                    fieldText = stringResource(Res.string.slots_save_as_replace),
                )
            }
            DescriptionText(stringResource(Res.string.slots_save_as_description))
        }
    }
}
