package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_folder_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Просьба выбрать папку игры или подтвердить доступ к ней. */
@Composable
internal fun FolderPrompt(description: String, button: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL)) {
        SectionHeader(stringResource(Res.string.slots_folder_title), Modifier.weight(1f), description = description)
        ActionButton(button, onClick = onClick)
    }
}
