package com.layzen.bdotools.core.language

/**
 * Язык интерфейса. Новый язык — новый элемент здесь и папка `values-<tag>` со строками в каждом модуле с UI.
 *
 * @property tag код языка по BCP 47 — он же суффикс папки ресурсов (`values-en`)
 * @property nativeName название языка на нём самом; не переводится, поэтому лежит здесь, а не в `strings.xml`
 */
enum class AppLanguage(val tag: String, val nativeName: String) {
    Russian("ru", "Русский"),
    English("en", "English"),
    ;

    companion object {
        /** Язык для тех, чей язык системы приложение не знает. */
        val Fallback = English

        /** Язык по коду [tag] (`ru`, `en-US`, `ru_RU`) или `null`, если такого языка в приложении нет. */
        fun fromTag(tag: String?): AppLanguage? {
            val language = tag?.substringBefore('-')?.substringBefore('_')?.lowercase()
            return entries.firstOrNull { it.tag == language }
        }
    }
}
