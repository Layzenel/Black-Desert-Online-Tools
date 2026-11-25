package com.layzen.bdotools.core.theme

/** Тема окон, которая помнится между запусками: на сайте — `localStorage`, на Desktop — настройки программы. */
interface ThemeSettings {
    /** Код темы, выбранной пользователем, или `null`, если он ещё не выбирал. */
    fun saved(): String?

    /** Запоминает тему по её коду [id]. */
    fun save(id: String)
}

/** Реализация [ThemeSettings] текущей платформы. */
internal expect fun createThemeSettings(): ThemeSettings
