package com.layzen.bdotools.feature.portraitconstructor.presentation.controller

import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemosInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.memoChanges
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NotesSyncDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.toNotice

/**
 * Синхронизация надписей портретов с заметками окна «Просмотр персонажей аккаунта» игры: окно показывает, какие
 * заметки изменятся, и по подтверждению записывает их. Заметки персонажей без надписи не трогаются.
 * Пока игра запущена, записывать нельзя: при смене персонажа, телепорте или выходе она перезапишет файл заметками из своей памяти.
 */
internal class NotesSyncController(
    private val context: PortraitsContext,
    private val memos: MemosInteractor,
) {
    /** Открывает окно или перечитывает его: заметки и то, запущена ли игра. */
    fun open() {
        val account = context.current.accountId ?: return
        val confirmed = context.current.notesSyncDialog?.gameClosedConfirmed == true
        context.update { it.copy(notesSyncDialog = NotesSyncDialogState(gameClosedConfirmed = confirmed)) }
        context.launch(onError = { e -> close().also { context.showNotice(e.toNotice()) } }) {
            val running = memos.isGameRunning()
            val current = memos.memos(account)
            val state = context.current
            val captions = state.portraits.map { it.characterId to state.decorationOf(it.characterId).caption }
            updateDialog { it.copy(changes = memoChanges(captions, current), gameRunning = running) }
        }
    }

    /** Запоминает отметку пользователя о том, что игра закрыта. */
    fun confirmGameClosed(checked: Boolean) = updateDialog { it.copy(gameClosedConfirmed = checked) }

    /** Закрывает окно без записи. */
    fun close() = context.update { it.copy(notesSyncDialog = null) }

    /** Записывает показанные изменения заметок, если [NotesSyncDialogState.canApply], и закрывает окно. */
    fun apply() {
        val account = context.current.accountId ?: return
        val dialog = context.current.notesSyncDialog ?: return
        val changes = dialog.changes.orEmpty()
        if (!dialog.canApply) return
        close()
        context.launch { context.showNotice(Notice(NoticeKind.NotesSynced, memos.write(account, changes))) }
    }

    private fun updateDialog(transform: (NotesSyncDialogState) -> NotesSyncDialogState) =
        context.update { it.copy(notesSyncDialog = it.notesSyncDialog?.let(transform)) }
}
