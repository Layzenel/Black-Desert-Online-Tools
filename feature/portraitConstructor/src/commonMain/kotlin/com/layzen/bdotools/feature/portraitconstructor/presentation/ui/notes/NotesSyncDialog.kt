package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemoChange
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NotesSyncDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitItem
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_cancel
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_apply
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_backup
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_changes
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_game_closed
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_game_running
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_loading
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_none
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_none_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_recheck
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_title
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_warning
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Окно синхронизации: что делает синхронизация, где в игре окно с заметками, предупреждение закрыть игру
 * и список заметок, которые изменятся. «Синхронизировать» есть, только когда менять что-то нужно.
 */
@Composable
internal fun NotesSyncDialog(state: NotesSyncDialogState, portraits: List<PortraitItem>, onEvent: (PortraitsEvent) -> Unit) {
    val changes = state.changes
    val close = { onEvent(PortraitsEvent.CloseNotesSync) }
    ModalWindow(
        title = stringResource(Res.string.portraits_notes_title),
        onDismissRequest = close,
        modifier = Modifier.width(PortraitsDimens.notesDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = close)
            when {
                state.gameRunning == true ->
                    ActionButton(stringResource(Res.string.portraits_notes_recheck), onClick = { onEvent(PortraitsEvent.OpenNotesSync) })
                !changes.isNullOrEmpty() -> ActionButton(
                    stringResource(Res.string.portraits_notes_apply),
                    onClick = { onEvent(PortraitsEvent.ApplyNotesSync) },
                    enabled = state.canApply,
                )
            }
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            DescriptionText(stringResource(Res.string.portraits_notes_description))
            GameWindowHint()
            if (changes != null) GameStatus(state, onEvent)
            when {
                changes == null -> LoadingRow(stringResource(Res.string.portraits_notes_loading))
                changes.isEmpty() -> {
                    DescriptionText(stringResource(Res.string.portraits_notes_none))
                    if (state.gameRunning != false) DescriptionText(stringResource(Res.string.portraits_notes_none_hint))
                }
                else -> {
                    ChangeList(changes, portraits)
                    DescriptionText(stringResource(Res.string.portraits_notes_backup))
                }
            }
        }
    }
}

/**
 * Закрыта ли игра. Программа проверяет процесс сама и при запущенной игре просит её закрыть; сайт этого не может,
 * поэтому просит подтвердить галочкой.
 */
@Composable
private fun GameStatus(state: NotesSyncDialogState, onEvent: (PortraitsEvent) -> Unit) {
    when (state.gameRunning) {
        true -> WarningText(stringResource(Res.string.portraits_notes_game_running))
        false -> Unit
        null -> {
            WarningText(stringResource(Res.string.portraits_notes_warning))
            val toggle = { onEvent(PortraitsEvent.ConfirmGameClosed(!state.gameClosedConfirmed)) }
            Row(
                Modifier.clickable(interactionSource = null, indication = null, onClick = toggle).pointerHoverIcon(PointerIcon.Hand),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckBox(state.gameClosedConfirmed, onCheckedChange = { onEvent(PortraitsEvent.ConfirmGameClosed(it)) })
                Text(stringResource(Res.string.portraits_notes_game_closed), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
private fun ChangeList(changes: List<MemoChange>, portraits: List<PortraitItem>) {
    val images = portraits.associate { it.characterId to it.image }
    SectionHeader(stringResource(Res.string.portraits_notes_changes, changes.size))
    Column(
        Modifier.heightIn(max = PortraitsDimens.notesListMaxHeight).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        changes.forEach { MemoChangeCard(it, images[it.characterId]) }
    }
}
