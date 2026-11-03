package com.layzen.bdotools.feature.portraits.domain

/** Надпись и значки, которые рисуются поверх чистого портрета. */
data class PortraitDecoration(
    val caption: String = "",
    val badges: Map<BadgeSlot, BadgeIcon> = emptyMap(),
) {
    val isEmpty: Boolean get() = caption.isBlank() && badges.isEmpty()

    fun withCaption(text: String) = copy(caption = normalizeCaption(text))

    fun withBadge(slot: BadgeSlot, icon: BadgeIcon?) =
        copy(badges = if (icon == null) badges - slot else badges + (slot to icon))

    companion object {
        const val CAPTION_MAX_LENGTH = 24
        const val CAPTION_MAX_LINES = 2

        /** Одна строка без лишних пробелов и не длиннее [CAPTION_MAX_LENGTH]. Переносы расставляет отрисовка. */
        fun normalizeCaption(text: String): String =
            text.replace(Regex("\\s+"), " ").trimStart().take(CAPTION_MAX_LENGTH)
    }
}
