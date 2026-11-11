package com.layzen.bdotools.core.account

/** Выбранный аккаунт, который помнится между запусками: на сайте — в `localStorage`, на Desktop — в настройках. */
interface AccountChoice {
    /** ID аккаунта, выбранного в прошлый раз, или `null`, если его ещё не выбирали. */
    fun load(): String?

    /** Запоминает выбранный аккаунт по его [id]. */
    fun save(id: String)
}

/** Реализация [AccountChoice] текущей платформы. */
internal expect fun createAccountChoice(): AccountChoice
