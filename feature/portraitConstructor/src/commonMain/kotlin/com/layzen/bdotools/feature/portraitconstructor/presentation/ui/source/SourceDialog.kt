package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.SourceDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.notice.noticeText
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_cancel
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_clipboard
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_clipboard_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_clipboard_paste
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_device
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_device_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_device_pick
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_loading
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_title_all
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_title_one
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_url
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_url_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_url_load
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_source_url_placeholder
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Выбор, откуда взять новое изображение: файл с устройства, буфер обмена или ссылка. */
@Composable
internal fun SourceDialog(state: SourceDialogState, onEvent: (PortraitsEvent) -> Unit) {
    val title = when (state.target) {
        ReplaceTarget.All -> Res.string.portraits_source_title_all
        is ReplaceTarget.One -> Res.string.portraits_source_title_one
    }
    ModalWindow(
        title = stringResource(title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseSource) },
        modifier = Modifier.width(PortraitsDimens.sourceDialogWidth),
        dismissible = !state.isLoading,
        footer = {
            ActionButton(
                stringResource(Res.string.portraits_cancel),
                onClick = { onEvent(PortraitsEvent.CloseSource) },
                enabled = !state.isLoading,
            )
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
                SectionHeader(
                    stringResource(Res.string.portraits_source_device),
                    Modifier.weight(1f),
                    description = stringResource(Res.string.portraits_source_device_description),
                )
                ActionButton(
                    stringResource(Res.string.portraits_source_device_pick),
                    onClick = { onEvent(PortraitsEvent.PickFromDevice) },
                    enabled = !state.isLoading,
                )
            }
            SectionDivider()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
                SectionHeader(
                    stringResource(Res.string.portraits_source_clipboard),
                    Modifier.weight(1f),
                    description = stringResource(Res.string.portraits_source_clipboard_description),
                )
                ActionButton(
                    stringResource(Res.string.portraits_source_clipboard_paste),
                    onClick = { onEvent(PortraitsEvent.PasteFromClipboard) },
                    enabled = !state.isLoading,
                )
            }
            SectionDivider()
            SectionHeader(
                stringResource(Res.string.portraits_source_url),
                description = stringResource(Res.string.portraits_source_url_description),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                InputField(
                    value = state.url,
                    onValueChange = { onEvent(PortraitsEvent.UrlChanged(it)) },
                    placeholder = stringResource(Res.string.portraits_source_url_placeholder),
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    stringResource(Res.string.portraits_source_url_load),
                    onClick = { onEvent(PortraitsEvent.LoadFromUrl) },
                    enabled = !state.isLoading && state.url.isNotBlank(),
                    modifier = Modifier.defaultMinSize(minWidth = PortraitsDimens.urlButtonMinWidth),
                )
            }
            if (state.isLoading) LoadingRow(stringResource(Res.string.portraits_source_loading))
            state.error?.let { WarningText(noticeText(it)) }
        }
    }
}
