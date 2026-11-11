package com.layzen.bdotools.core.account

/**
 * Подпись персонажа, которую игрок дал ему в «Конструкторе портретов»: файл украшений портрета
 * `BDO Tools\portraitConstructor\<аккаунт>\Current portraits\<CharacterNo>.txt` (строки `caption=Рыбак`,
 * `badge=class-warrior`). Другие тулзы показывают по ней, что это за персонаж: имён в файлах игры нет.
 *
 * @property caption подпись на портрете; `null` — подписи нет
 * @property classKey класс со значка портрета (`CharacterClass.key`); `null` — значок не класса или его нет
 */
data class CharacterLabel(val caption: String?, val classKey: String?) {

    companion object {
        private const val CAPTION_KEY = "caption"
        private const val BADGE_KEY = "badge"
        private const val CLASS_BADGE_PREFIX = "class-"

        /** Читает файл украшений портрета; неизвестные строки пропускаются (формат — `DecorationFile` портретов). */
        fun parse(text: String): CharacterLabel {
            val values = text.lineSequence()
                .map { it.trimEnd('\r') }
                .filter { '=' in it }
                .associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
            return CharacterLabel(
                caption = values[CAPTION_KEY]?.takeIf { it.isNotEmpty() },
                classKey = values[BADGE_KEY]?.takeIf { it.startsWith(CLASS_BADGE_PREFIX) }?.removePrefix(CLASS_BADGE_PREFIX),
            )
        }
    }
}
