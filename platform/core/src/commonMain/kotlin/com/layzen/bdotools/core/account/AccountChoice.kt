package com.layzen.bdotools.core.account

/** Выбранный аккаунт, который помнится между запусками: на сайте — в `localStorage`, на Desktop — в настройках. */
interface AccountChoice {
    fun load(): String?

    fun save(id: String)
}

/** Реализация [AccountChoice] текущей платформы. */
internal expect fun createAccountChoice(): AccountChoice
