package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.label
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.RestoreDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.previewKey
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid.PortraitGridLayout
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid.PortraitTile
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_delete_marked
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_backups
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_check_all
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_description_all
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_portraits
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_uncheck_all
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_warning
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Содержимое окна восстановления всех портретов: список копий, превью и отметки портретов. */
@Composable
internal fun AllRestore(state: RestoreDialogState, onEvent: (PortraitsEvent) -> Unit) {
    val backup = state.selectedBackup
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL)) {
        Column(
            Modifier
                .width(PortraitsDimens.restoreListWidth)
                .heightIn(max = PortraitsDimens.restoreContentMaxHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(stringResource(Res.string.portraits_restore_backups), Modifier.padding(bottom = AppTheme.dimens.spaceS))
            state.backups.forEach { item ->
                // галочка внутри строки отмечает копию для удаления, клик по остальной строке открывает её
                MenuItem(
                    text = item.label,
                    hotkey = item.characterIds.size.toString(),
                    selected = item.name == state.selected,
                    onClick = { onEvent(PortraitsEvent.SelectBackup(item.name)) },
                    icon = {
                        CheckBox(
                            checked = item.name in state.marked,
                            onCheckedChange = { onEvent(PortraitsEvent.ToggleBackupMark(item.name)) },
                        )
                    },
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            if (backup == null) return@Column
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                SectionHeader(
                    stringResource(Res.string.portraits_restore_portraits, backup.characterIds.size),
                    Modifier.weight(1f),
                    description = stringResource(Res.string.portraits_restore_description_all),
                    warning = stringResource(Res.string.portraits_restore_warning),
                )
                ActionButton(stringResource(Res.string.portraits_restore_check_all), onClick = { onEvent(PortraitsEvent.CheckAllRestorePortraits(true)) })
                ActionButton(stringResource(Res.string.portraits_restore_uncheck_all), onClick = { onEvent(PortraitsEvent.CheckAllRestorePortraits(false)) })
                if (state.marked.isNotEmpty()) {
                    ActionButton(
                        stringResource(Res.string.portraits_delete_marked, state.marked.size),
                        onClick = { onEvent(PortraitsEvent.RequestDeleteBackups) },
                    )
                }
            }
            val ids = state.previews.keys.filter { it.startsWith(backup.name + "/") }.map { it.substringAfter('/') }
                .let { loaded -> loaded + (backup.characterIds - loaded.toSet()).sorted() }
            PortraitGridLayout(
                items = ids,
                modifier = Modifier.heightIn(max = PortraitsDimens.restoreContentMaxHeight).verticalScroll(rememberScrollState()),
            ) { id ->
                PortraitTile(
                    image = state.previews[previewKey(backup.name, id)],
                    selected = id in state.checked,
                    dimmed = id !in state.checked,
                    onClick = { onEvent(PortraitsEvent.ToggleRestorePortrait(id)) },
                )
            }
        }
    }
}
