package com.layzen.bdotools.core.keyhint

/** Код клавиши Shift в раскладке игры (виртуальный код Windows). */
const val KEY_CODE_SHIFT = 16

/** Код левой кнопки мыши в раскладке игры. */
const val KEY_CODE_LMB = 1

/** Код правой кнопки мыши в раскладке игры. */
const val KEY_CODE_RMB = 2

/**
 * Как показывать клавиши, которые можно заменить картинкой или сокращением.
 *
 * @property shift вид Shift
 * @property lmb вид левой кнопки мыши (сокращение — «Л» / «L»)
 * @property rmb вид правой кнопки мыши (сокращение — «П» / «R»)
 */
data class KeyHints(
    val shift: KeyHintStyle = KeyHintStyle.Text,
    val lmb: KeyHintStyle = KeyHintStyle.Text,
    val rmb: KeyHintStyle = KeyHintStyle.Text,
) {
    /** Вид клавиши с кодом [keyCode]; остальные клавиши всегда показываются словом. */
    fun styleOf(keyCode: Int): KeyHintStyle = when (keyCode) {
        KEY_CODE_SHIFT -> shift
        KEY_CODE_LMB -> lmb
        KEY_CODE_RMB -> rmb
        else -> KeyHintStyle.Text
    }

    /** Строка для сохранения: виды Shift, ЛКМ и ПКМ через запятую. */
    fun encode(): String = listOf(shift, lmb, rmb).joinToString(",") { it.name.lowercase() }

    companion object {
        /** Всё словом — как было до настройки. */
        val Default = KeyHints()

        /** Разбирает строку из [encode]; пустую, `null` или испорченную читает словом — по каждой недостающей клавише. */
        fun decode(text: String?): KeyHints {
            val styles = text.orEmpty().split(',').map { part -> KeyHintStyle.entries.firstOrNull { it.name.equals(part, ignoreCase = true) } }

            fun at(index: Int) = styles.getOrNull(index) ?: KeyHintStyle.Text
            return KeyHints(at(0), at(1), at(2))
        }
    }
}

