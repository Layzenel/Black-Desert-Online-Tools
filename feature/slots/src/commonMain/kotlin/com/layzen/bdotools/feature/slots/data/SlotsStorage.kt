package com.layzen.bdotools.feature.slots.data

/**
 * Файлы в папке `Документы\Black Desert`: настройки игры и файлы тулзы в `BDO Tools\slots`.
 * Все методы, кроме [access], требуют [FolderAccess.Granted].
 */
interface SlotsStorage {
    suspend fun access(): FolderAccess

    /** Открывает выбор папки. Подходит `Black Desert` или `Документы`, в которой она лежит. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Повторно запрашивает разрешение на выбранную раньше папку. Вызывать только по клику. */
    suspend fun requestPermission(): FolderAccess.Granted

    /** Папки `UserCache\<аккаунт>`, где есть `gamevariable.xml`; первой — с самым свежим файлом. */
    suspend fun accounts(): List<String>

    suspend fun readGameVariable(account: String): GameFile

    /**
     * Копирует текущий файл в `BDO Tools\slots\backups` и записывает [text]. Если файл изменился
     * после [expectedModified] — [SlotsError.FileChanged], ничего не записывается. Возвращает путь копии.
     */
    suspend fun writeGameVariable(account: String, text: String, expectedModified: Long): String

    /** Файлы настроек персонажей `UserCache\<аккаунт>\11\<персонаж>\gamevariable.xml`, пути от папки аккаунта. */
    suspend fun characterFiles(account: String): List<String>

    suspend fun readCharacterFile(account: String, path: String): String

    /** Копирует файл персонажа в `BDO Tools\slots\backups` и записывает [text]. */
    suspend fun writeCharacterFile(account: String, path: String, text: String)

    /** Файлы `*.json` в `BDO Tools\slots\presets`. */
    suspend fun presetFiles(): List<String>

    suspend fun readPreset(fileName: String): String

    suspend fun writePreset(fileName: String, text: String)

    suspend fun deletePreset(fileName: String)

    /** Картинка-подложка редактора; `null`, если её не выбирали. */
    suspend fun readBackground(): ByteArray?

    suspend fun writeBackground(bytes: ByteArray?)
}
