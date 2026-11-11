package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Знаки Windows-1251 для байтов 0x80–0xBF. Байты 0xC0–0xFF — подряд «А»…«я» (U+0410…U+044F), байты до 0x80 — ASCII.
 * Неиспользуемый байт 0x98 — «�».
 */
private const val CP1251_HIGH =
    "ЂЃ‚ѓ„…†‡€‰Љ‹ЊЌЋЏђ‘’“”•–—�™љ›њќћџ ЎўЈ¤Ґ¦§Ё©Є«¬­®Ї°±Ііґµ¶·ё№є»јЅѕї"

private const val CYRILLIC_START = 0xC0
private const val UNUSED_BYTE = 0x98

/** Чем заменяется знак, которого нет в Windows-1251 (эмодзи, иероглифы). */
private const val REPLACEMENT = '?'

/**
 * Текст в Windows-1251, прочитанный по символу на байт (Latin-1), как его читает хранилище файлов игры:
 * каждый символ [latin1] — один байт. Так игра пишет надписи (`Memo`) в `gamevariable.xml`.
 */
fun decodeCp1251(latin1: String): String = buildString(latin1.length) {
    for (char in latin1) {
        val byte = char.code and 0xFF
        append(
            when {
                byte < 0x80 -> char
                byte >= CYRILLIC_START -> 'А' + (byte - CYRILLIC_START)
                else -> CP1251_HIGH[byte - 0x80]
            },
        )
    }
}

/**
 * Обратное к [decodeCp1251]: [text] в байтах Windows-1251, записанных по символу на байт, — так хранилище
 * пишет файл игры байт в байт. Знаков вне Windows-1251 игра не покажет: они заменяются на `?`.
 */
fun encodeCp1251(text: String): String = buildString(text.length) {
    for (char in text) append(cp1251Byte(char)?.toChar() ?: REPLACEMENT)
}

/** Есть ли знак [char] в Windows-1251: только такие игра покажет в надписи. */
fun isCp1251(char: Char): Boolean = cp1251Byte(char) != null

private fun cp1251Byte(char: Char): Int? = when {
    char.code < 0x80 -> char.code
    char in 'А'..'я' -> CYRILLIC_START + (char - 'А')
    else -> CP1251_HIGH.indexOf(char).takeIf { it >= 0 && it != UNUSED_BYTE - 0x80 }?.plus(0x80)
}
