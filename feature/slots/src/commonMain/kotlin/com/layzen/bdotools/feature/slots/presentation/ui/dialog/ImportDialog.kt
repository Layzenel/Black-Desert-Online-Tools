package com.layzen.bdotools.feature.slots.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.domain.LayoutError
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_cancel
import com.layzen.bdotools.feature.slots.resources.slots_import_next
import com.layzen.bdotools.feature.slots.resources.slots_import_description
import com.layzen.bdotools.feature.slots.resources.slots_import_file
import com.layzen.bdotools.feature.slots.resources.slots_import_not_json
import com.layzen.bdotools.feature.slots.resources.slots_import_title
import com.layzen.bdotools.feature.slots.resources.slots_import_unsupported_version
import com.layzen.bdotools.feature.slots.resources.slots_import_wrong_format
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Сколько строк видно в поле для вставки JSON. */
private const val IMPORT_LINES = 10

/** Вставка JSON раскладки или выбор файла. Ошибка чтения показывается под полем. */
@Composable
internal fun ImportDialog(dialog: SlotsDialog.Import, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    ModalWindow(
        title = stringResource(Res.string.slots_import_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_import_file), onClick = { onEvent(SlotsEvent.ImportFromFile) })
            Spacer(Modifier.weight(1f))
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(
                stringResource(Res.string.slots_import_next),
                onClick = { onEvent(SlotsEvent.ConfirmImport) },
                enabled = dialog.text.isNotBlank(),
            )
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            DescriptionText(stringResource(Res.string.slots_import_description))
            InputField(
                value = dialog.text,
                onValueChange = { onEvent(SlotsEvent.ImportTextChanged(it)) },
                modifier = Modifier.fillMaxWidth().height(SlotsDimens.importTextHeight),
                minLines = IMPORT_LINES,
            )
            dialog.error?.let { WarningText(stringResource(it.message)) }
        }
    }
}

private val LayoutError.message: StringResource
    get() = when (this) {
        LayoutError.NotJson -> Res.string.slots_import_not_json
        LayoutError.WrongFormat -> Res.string.slots_import_wrong_format
        LayoutError.UnsupportedVersion -> Res.string.slots_import_unsupported_version
    }
