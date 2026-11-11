package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.game.GameFile

/**
 * Файлы в папке `Документы\Black Desert`: настройки игры и файлы тулзы в папке аккаунта
 * `BDO Tools\uiEditor\<account>\` — `backups`, `presets`, `background`.
 * Все методы, кроме [access], требуют [FolderAccess.Granted].
 */
interface SlotsStorage {
    /** Текущий доступ к папке `Black Desert`: выдан, нужно подтвердить или папку ещё не выбирали. */
    suspend fun access(): FolderAccess

    /** Открывает выбор папки. Подходит `Black Desert` или `Документы`, в которой она лежит. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Повторно запрашивает разрешение на выбранную раньше папку. Вызывать только по клику. */
    suspend fun requestPermission(): FolderAccess.Granted

    /** Папки `UserCache\<аккаунт>`, где есть `gamevariable.xml`; первой — с самым свежим файлом. */
    suspend fun accounts(): List<String>

    /** Читает общий `gamevariable.xml` аккаунта [account]. */
    suspend fun readGameVariable(account: String): GameFile

    /**
     * Копирует текущий файл в `<account>\backups` и записывает [text]. Если файл изменился
     * после [expectedModified] — [SlotsError.FileChanged], ничего не записывается. Возвращает путь копии.
     */
    suspend fun writeGameVariable(account: String, text: String, expectedModified: Long): String

    /** Файлы настроек персонажей `UserCache\<аккаунт>\11\<персонаж>\gamevariable.xml`, пути от папки аккаунта. */
    suspend fun characterFiles(account: String): List<String>

    /** Читает файл настроек персонажа по пути [path] от папки аккаунта [account]. */
    suspend fun readCharacterFile(account: String, path: String): String

    /** Копирует файл персонажа в `<account>\backups` и записывает [text]. */
    suspend fun writeCharacterFile(account: String, path: String, text: String)

    /** Файлы `*.json` в `<account>\presets`. */
    suspend fun presetFiles(account: String): List<String>

    /** Читает файл пресета [fileName] из папки пресетов аккаунта [account]. */
    suspend fun readPreset(account: String, fileName: String): String

    /** Записывает [text] в файл пресета [fileName] аккаунта [account]; существующий перезаписывает. */
    suspend fun writePreset(account: String, fileName: String, text: String)

    /** Удаляет файл пресета [fileName] аккаунта [account]. */
    suspend fun deletePreset(account: String, fileName: String)

    /** Картинка-подложка редактора аккаунта — файл `<account>\background\image`; `null`, если её не выбирали. */
    suspend fun readBackground(account: String): ByteArray?

    /** Сохраняет картинку-подложку аккаунта [account]; `null` убирает её. */
    suspend fun writeBackground(account: String, bytes: ByteArray?)

    /**
     * Переносит данные прошлых версий из `BDO Tools\slots` в `BDO Tools\uiEditor\<аккаунт>\…`: где аккаунт известен
     * (папка `<аккаунт>` или копия `gamevariable_<аккаунт>_…`) — ему, остальное (пресеты прямо в `presets`, файл
     * `background`) — аккаунту [account]. Совпавший файл не перезаписывается и остаётся на старом месте.
     */
    suspend fun adoptLooseFiles(account: String)
}
