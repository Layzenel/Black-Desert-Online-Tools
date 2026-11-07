package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess

/**
 * Файлы игры в папке `Документы\Black Desert`. Все методы, кроме [access], требуют [FolderAccess.Granted].
 * Резервные копии и избранное лежат по аккаунтам: `FaceTexture_backup\<account>\…` и `Favorite FaceTexture\<account>\…`;
 * `account = null` — прямо в этих папках, как до появления аккаунтов.
 */
interface PortraitsStorage {
    suspend fun access(): FolderAccess

    /** Открывает выбор папки. Подходит `Black Desert` или `Документы`, в которой она лежит. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Повторно запрашивает разрешение на выбранную раньше папку. Вызывать только по клику. */
    suspend fun requestPermission(): FolderAccess.Granted

    /** ID персонажей, для которых есть `FaceTexture\<ID>.bmp`. */
    suspend fun portraitIds(): List<String>

    suspend fun readPortrait(characterId: String): ByteArray

    /** Создаёт пустую папку `FaceTexture_backup\<account>\<дата-время>` и возвращает её имя. */
    suspend fun createBackup(account: String?): String

    suspend fun writePortrait(characterId: String, bmp: ByteArray)

    /** Список папок `FaceTexture_backup\<account>` в формате [com.layzen.bdotools.feature.portraitconstructor.domain.parseBackupList]. */
    suspend fun backupListing(account: String?): String

    /** Файл из резервной копии [backup]; `null`, если файла нет. */
    suspend fun readBackupFile(account: String?, backup: String, name: String): ByteArray?

    suspend fun writeBackupFile(account: String?, backup: String, name: String, bytes: ByteArray)

    /** Удаляет папку `FaceTexture_backup\<account>\<backup>` целиком. */
    suspend fun deleteBackup(account: String?, backup: String)

    /** Файлы в `Black Desert\BDO Tools\portraits`: чистые портреты и украшения. */
    suspend fun toolFileNames(): List<String>

    /** `null`, если файла нет. */
    suspend fun readToolFile(name: String): ByteArray?

    suspend fun writeToolFile(name: String, bytes: ByteArray)

    suspend fun deleteToolFile(name: String)

    /** Создаёт пустую папку `Favorite FaceTexture\<account>\<дата-время>` и возвращает её имя. */
    suspend fun createFavoriteSet(account: String?): String

    /** Список папок `Favorite FaceTexture\<account>` в том же формате, что [backupListing]. */
    suspend fun favoriteListing(account: String?): String

    /** Файл из папки избранного [set]; `null`, если файла нет. */
    suspend fun readFavoriteFile(account: String?, set: String, name: String): ByteArray?

    suspend fun writeFavoriteFile(account: String?, set: String, name: String, bytes: ByteArray)

    /** Удаляет папку `Favorite FaceTexture\<account>\<set>` целиком. */
    suspend fun deleteFavoriteSet(account: String?, set: String)

    /**
     * Переносит в папку аккаунта [account] копии и наборы избранного, лежащие прямо в `FaceTexture_backup` и
     * `Favorite FaceTexture` (из версий до аккаунтов): это папки, чьё имя не номер аккаунта. Совпавшее имя получает суффикс.
     */
    suspend fun adoptLooseFolders(account: String)

    /** `UserCache\<account>\gameVariable_lobby.xml`; `null`, если файла нет. */
    suspend fun readLobbyVariables(account: String): ByteArray?

    suspend fun writeLobbyVariables(account: String, bytes: ByteArray)

    /** Сохраняет [bytes] в `BDO Tools\lobby_backup\<account>\<дата-время>.xml` и возвращает путь к копии. */
    suspend fun backupLobbyVariables(account: String, bytes: ByteArray): String

    /**
     * Запущена ли игра: пока она работает, её файлы настроек перезапишутся данными из её памяти при смене персонажа, телепорте или выходе.
     * `null` — платформа проверить не может (браузер не видит процессы).
     */
    suspend fun isGameRunning(): Boolean?
}
