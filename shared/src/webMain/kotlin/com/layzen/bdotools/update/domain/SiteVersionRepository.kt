package com.layzen.bdotools.update.domain

/** Версии сайта: открытой страницы и выложенной сейчас. */
internal interface SiteVersionRepository {

    /** Версия открытой страницы, её вшивает сборка; `null` — сборка разработчика. */
    val ownVersion: String?

    /** Номер версии, выложенной на сайт сейчас, или `null`, если узнать его не удалось. */
    suspend fun deployedVersion(): String?
}
