package com.layzen.bdotools.feature.uieditor.data.skill

/**
 * Скачивание файлов сайта данных по HTTP: в браузере — `fetch`, на Desktop — `java.net.http`.
 * Ошибки сети и ответы не 200 — `null`, исключений нет: без справочника редактор работает дальше.
 */
interface SkillDownloads {

    /** Текст по адресу [url] или `null`. */
    suspend fun text(url: String): String?

    /** Байты по адресу [url] или `null`. */
    suspend fun bytes(url: String): ByteArray?
}

/** Реализация [SkillDownloads] своей платформы. */
internal expect fun createSkillDownloads(): SkillDownloads
