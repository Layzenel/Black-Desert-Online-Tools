package com.layzen.bdotools.feature.portraits.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete_confirm
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete_text
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete_title
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_apply_count
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_backups
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_check_all
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_current
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_description_all
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_description_one
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_empty
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_loading
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_portraits
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_title_all
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_title_one
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_uncheck_all
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_warning
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Spinner
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val UNCHECKED_ALPHA = 0.35f
private const val PORTRAIT_RATIO = PortraitTexture.WIDTH.toFloat() / PortraitTexture.HEIGHT

/**
 * Восстановление из `FaceTexture_backup`.
 * Для всех портретов: слева список копий, справа её портреты; отмеченные заменят текущие.
 * Для одного портрета: текущий портрет и его версии из всех копий, где он есть.
 */
@Composable
internal fun RestoreDialog(state: RestoreDialogState, portraits: List<PortraitItem>, onEvent: (PortraitsEvent) -> Unit) {
    val single = state.target as? ReplaceTarget.One
    val restoreCount = if (single != null) 1 else state.checked.size
    ModalWindow(
        title = stringResource(if (single != null) Res.string.portraits_restore_title_one else Res.string.portraits_restore_title_all),
        onDismissRequest = { onEvent(PortraitsEvent.CloseRestore) },
        modifier = Modifier.width(if (single != null) PortraitsDimens.restoreOneDialogWidth else PortraitsDimens.restoreDialogWidth),
        footer = {
            if (state.isLoadingPreviews) LoadingRow(stringResource(Res.string.portraits_restore_loading), Modifier.weight(1f))
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseRestore) })
            ActionButton(
                if (single != null) {
                    stringResource(Res.string.portraits_restore_apply)
                } else {
                    stringResource(Res.string.portraits_restore_apply_count, restoreCount)
                },
                onClick = { onEvent(PortraitsEvent.ApplyRestore) },
                enabled = state.selected != null && restoreCount > 0,
            )
        },
    ) {
        Box(Modifier.padding(AppTheme.dimens.spaceL)) {
            when {
                state.backups.isEmpty() -> DescriptionText(stringResource(Res.string.portraits_restore_empty))
                single != null -> SingleRestore(state, portraits.firstOrNull { it.characterId == single.characterId }, single.characterId, onEvent)
                else -> AllRestore(state, onEvent)
            }
        }
    }

    if (state.confirmDelete) DeleteBackupsConfirmation(state, onEvent)
}

@Composable
private fun AllRestore(state: RestoreDialogState, onEvent: (PortraitsEvent) -> Unit) {
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
                        stringResource(Res.string.portraits_backups_delete, state.marked.size),
                        onClick = { onEvent(PortraitsEvent.RequestDeleteBackups) },
                    )
                }
            }
            val ids = state.previews.keys.filter { it.startsWith(backup.name + "/") }.map { it.substringAfter('/') }
                .let { loaded -> loaded + (backup.characterIds - loaded.toSet()).sorted() }
            PreviewGrid(
                Modifier.heightIn(max = PortraitsDimens.restoreContentMaxHeight).verticalScroll(rememberScrollState()),
                items = ids,
            ) { id ->
                PreviewTile(
                    image = state.previews[previewKey(backup.name, id)],
                    selected = id in state.checked,
                    dimmed = id !in state.checked,
                    onClick = { onEvent(PortraitsEvent.ToggleRestorePortrait(id)) },
                )
            }
        }
    }
}

@Composable
private fun SingleRestore(state: RestoreDialogState, current: PortraitItem?, characterId: String, onEvent: (PortraitsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        DescriptionText(stringResource(Res.string.portraits_restore_description_one))
        WarningText(stringResource(Res.string.portraits_restore_warning))
        val items = listOf<String?>(null) + state.backups.map { it.name }
        PreviewGrid(
            Modifier.heightIn(max = PortraitsDimens.restoreContentMaxHeight).verticalScroll(rememberScrollState()),
            items = items,
            columns = PortraitsDimens.RESTORE_ONE_COLUMNS,
        ) { name ->
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                if (name == null) {
                    PreviewTile(image = current?.image, selected = false, dimmed = false, onClick = null)
                    Caption(stringResource(Res.string.portraits_restore_current))
                } else {
                    PreviewTile(
                        image = state.previews[previewKey(name, characterId)],
                        selected = name == state.selected,
                        dimmed = false,
                        onClick = { onEvent(PortraitsEvent.SelectBackup(name)) },
                    )
                    Caption(state.backups.first { it.name == name }.label)
                }
            }
        }
    }
}

/** Сетка как в окне выбора персонажа: не больше [columns] в ряду, неполный ряд прижат влево. */
@Composable
private fun <T> PreviewGrid(
    modifier: Modifier,
    items: List<T>,
    columns: Int = PortraitTexture.MAX_COLUMNS,
    cell: @Composable (T) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                row.forEach { item -> Box(Modifier.weight(1f)) { cell(item) } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PreviewTile(image: ImageBitmap?, selected: Boolean, dimmed: Boolean, onClick: (() -> Unit)?) {
    Tile(Modifier.fillMaxWidth().aspectRatio(PORTRAIT_RATIO), selected = selected, onClick = onClick) {
        if (image == null) {
            Spinner(Modifier.align(Alignment.Center), size = AppTheme.dimens.iconSize)
        } else {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Medium,
                modifier = Modifier.matchParentSize().alpha(if (dimmed) UNCHECKED_ALPHA else 1f),
            )
        }
    }
}

@Composable
private fun Caption(text: String) {
    Text(
        text,
        Modifier.fillMaxWidth(),
        style = AppTheme.typography.caption,
        color = AppTheme.colors.textMuted,
        textAlign = TextAlign.Center,
        maxLines = 2,
    )
}

/** Подтверждение удаления отмеченных копий: перечисляет их, потому что удаление необратимо. */
@Composable
private fun DeleteBackupsConfirmation(state: RestoreDialogState, onEvent: (PortraitsEvent) -> Unit) {
    val marked = state.backups.filter { it.name in state.marked }
    ModalWindow(
        title = stringResource(Res.string.portraits_backups_delete_title),
        onDismissRequest = { onEvent(PortraitsEvent.CancelDeleteBackups) },
        modifier = Modifier.width(PortraitsDimens.confirmDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CancelDeleteBackups) })
            ActionButton(stringResource(Res.string.portraits_backups_delete_confirm), onClick = { onEvent(PortraitsEvent.ConfirmDeleteBackups) })
        },
    ) {
        Column(
            Modifier
                .padding(AppTheme.dimens.spaceL)
                .heightIn(max = PortraitsDimens.confirmListMaxHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            WarningText(stringResource(Res.string.portraits_backups_delete_text, marked.size))
            marked.forEach { backup ->
                Text(backup.label, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
            }
        }
    }
}
