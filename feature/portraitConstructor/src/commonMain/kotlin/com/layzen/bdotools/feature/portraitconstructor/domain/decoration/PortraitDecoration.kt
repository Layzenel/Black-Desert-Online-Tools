package com.layzen.bdotools.feature.portraitconstructor.domain.decoration

/**
 * Надпись и значок (класс или занятие), которые рисуются поверх чистого портрета.
 *
 * @property captionColor цвет надписи, `0xRRGGBB`; `null` — цвет по умолчанию (кремовый, как текст игры)
 * @property badgeColor цвет значка, `0xRRGGBB`; `null` — цвет по умолчанию
 */
data class PortraitDecoration(
    val caption: String = "",
    val badge: SlotIcon? = null,
    val captionColor: Int? = null,
    val badgeColor: Int? = null,
) {
    /** `true`, если нет ни надписи, ни значка: такому портрету файл украшения не нужен. */
    val isEmpty: Boolean get() = caption.isBlank() && badge == null

    /** Копия с надписью [text], приведённой к виду по [normalizeCaption]. */
    fun withCaption(text: String) = copy(caption = normalizeCaption(text))

    /** Копия со значком [icon]; `null` убирает значок. */
    fun withBadge(icon: SlotIcon?) = copy(badge = icon)

    companion object {
        const val CAPTION_MAX_LENGTH = 24
        const val CAPTION_MAX_LINES = 2

        /** Одна строка без лишних пробелов и не длиннее [CAPTION_MAX_LENGTH]. Переносы расставляет отрисовка. */
        fun normalizeCaption(text: String): String =
            text.replace(Regex("\\s+"), " ").trimStart().take(CAPTION_MAX_LENGTH)
    }
}
