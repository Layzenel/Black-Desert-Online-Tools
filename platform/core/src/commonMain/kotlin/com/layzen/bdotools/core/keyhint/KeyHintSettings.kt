package com.layzen.bdotools.core.keyhint

/** Вид подсказок клавиш, который помнится между запусками: на сайте — `localStorage`, на Desktop — настройки программы. */
interface KeyHintSettings {
    /** Сохранённая строка из [KeyHints.encode] или `null`, если пользователь ещё не выбирал. */
    fun saved(): String?

    /** Запоминает строку [encoded] из [KeyHints.encode]. */
    fun save(encoded: String)
}

/** Реализация [KeyHintSettings] текущей платформы. */
internal expect fun createKeyHintSettings(): KeyHintSettings
