package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Сообщение пользователю. [argument] подставляется в текст (путь к копии, описание ошибки). */
data class Notice(val kind: NoticeKind, val argument: String = "") {
    /** `true` для сообщений об ошибке, то есть всех видов, кроме успешных. */
    val isError: Boolean get() = kind !in SUCCESS_KINDS

    private companion object {
        val SUCCESS_KINDS = setOf(
            NoticeKind.Saved, NoticeKind.DecorationSaved, NoticeKind.Restored, NoticeKind.BackupsDeleted,
            NoticeKind.FavoriteAdded, NoticeKind.FavoriteApplied, NoticeKind.Swapped,
            NoticeKind.FavoritesDeleted, NoticeKind.FavoriteReplaced, NoticeKind.ReorderHint, NoticeKind.NotesSynced,
        )
    }
}
