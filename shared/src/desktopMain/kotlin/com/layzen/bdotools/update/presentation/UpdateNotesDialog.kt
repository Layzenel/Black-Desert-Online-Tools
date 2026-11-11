package com.layzen.bdotools.update.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.update_install
import com.layzen.bdotools.resources.update_later
import com.layzen.bdotools.resources.update_notes_empty
import com.layzen.bdotools.resources.update_notes_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import com.layzen.bdotools.update.domain.UpdateInfo
import org.jetbrains.compose.resources.stringResource

/** Предложение обновиться: изменения новой версии (заголовки влитых PR с прошлой сборки), «Позже» и «Обновить». */
@Composable
internal fun UpdateNotesDialog(info: UpdateInfo, onClose: () -> Unit, onInstall: () -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.update_notes_title, info.version.toString()),
        onDismissRequest = onClose,
        modifier = Modifier.width(UpdateDimens.notesDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.update_later), onClick = onClose)
            ActionButton(stringResource(Res.string.update_install), onClick = onInstall)
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            if (info.notes.isEmpty()) DescriptionText(stringResource(Res.string.update_notes_empty))
            info.notes.forEach { note ->
                Text(note, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
            }
        }
    }
}
