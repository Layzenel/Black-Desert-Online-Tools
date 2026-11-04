package com.layzen.bdotools.feature.portraits.domain

/** Надпись и значок (класс или занятие), которые рисуются поверх чистого портрета. */
data class PortraitDecoration(
    val caption: String = "",
    val badge: SlotIcon? = null,
) {
    val isEmpty: Boolean get() = caption.isBlank() && badge == null

    fun withCaption(text: String) = copy(caption = normalizeCaption(text))

    fun withBadge(icon: SlotIcon?) = copy(badge = icon)

    companion object {
        const val CAPTION_MAX_LENGTH = 24
        const val CAPTION_MAX_LINES = 2

        /** Одна строка без лишних пробелов и не длиннее [CAPTION_MAX_LENGTH]. Переносы расставляет отрисовка. */
        fun normalizeCaption(text: String): String =
            text.replace(Regex("\\s+"), " ").trimStart().take(CAPTION_MAX_LENGTH)
    }
}
