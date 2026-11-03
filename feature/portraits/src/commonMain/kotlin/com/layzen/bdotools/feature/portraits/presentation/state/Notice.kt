package com.layzen.bdotools.feature.portraits.presentation.state

/** Сообщение пользователю. [argument] подставляется в текст (путь к копии, описание ошибки). */
data class Notice(val kind: NoticeKind, val argument: String = "") {
    val isError: Boolean get() = kind !in SUCCESS_KINDS

    private companion object {
        val SUCCESS_KINDS = setOf(NoticeKind.Saved, NoticeKind.DecorationSaved, NoticeKind.Restored, NoticeKind.BackupsDeleted)
    }
}
