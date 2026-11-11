package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemoChange

/**
 * Окно синхронизации надписей с заметками игры. [changes] — `null`, пока заметки читаются; пустой — менять нечего.
 * [gameRunning] — запущена ли игра; `null` — проверить нельзя (браузер), тогда пользователь подтверждает
 * [gameClosedConfirmed], что игра закрыта.
 */
data class NotesSyncDialogState(
    val changes: List<MemoChange>? = null,
    val gameRunning: Boolean? = null,
    val gameClosedConfirmed: Boolean = false,
) {
    /** Записывать можно, когда есть что и игра точно закрыта. */
    val canApply: Boolean
        get() = !changes.isNullOrEmpty() && (gameRunning == false || gameRunning == null && gameClosedConfirmed)
}
