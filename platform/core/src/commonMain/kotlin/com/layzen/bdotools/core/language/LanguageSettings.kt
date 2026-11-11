package com.layzen.bdotools.core.language

/** Язык, который помнится между запусками, и язык системы: на сайте — `localStorage` и браузер, на Desktop — настройки и ОС. */
interface LanguageSettings {
    /** Код языка, выбранного пользователем, или `null`, если он ещё не выбирал. */
    fun saved(): String?

    /** Запоминает выбранный язык по его коду [tag]. */
    fun save(tag: String)

    /** Код языка системы или браузера. */
    fun system(): String
}

/** Реализация [LanguageSettings] текущей платформы. */
internal expect fun createLanguageSettings(): LanguageSettings
