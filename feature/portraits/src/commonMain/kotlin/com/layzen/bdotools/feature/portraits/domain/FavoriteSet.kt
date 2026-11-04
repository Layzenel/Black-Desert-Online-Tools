package com.layzen.bdotools.feature.portraits.domain

/**
 * Набор избранного — папка `Favorite FaceTexture\<name>` с портретами [characterIds].
 * [title] задаёт пользователь; без него набор подписан датой сохранения из имени папки.
 */
data class FavoriteSet(val name: String, val characterIds: Set<String>, val title: String? = null) {
    /** Дата сохранения из имени папки: `2026-11-03 12:00:05`. */
    val dateLabel: String get() = BackupInfo(name, characterIds).label

    val label: String get() = title ?: dateLabel

    companion object {
        const val TITLE_MAX_LENGTH = 40

        /** Название в одну строку без лишних пробелов; пустое — `null`, то есть подпись датой. */
        fun normalizeTitle(text: String): String? =
            text.replace(Regex("""\s+"""), " ").trim().take(TITLE_MAX_LENGTH).ifEmpty { null }
    }
}
