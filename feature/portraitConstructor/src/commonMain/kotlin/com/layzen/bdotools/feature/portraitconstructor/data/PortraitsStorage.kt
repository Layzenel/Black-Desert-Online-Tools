package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess

/** Файлы игры в папке `Документы\Black Desert`. Все методы, кроме [access], требуют [FolderAccess.Granted]. */
interface PortraitsStorage {
    suspend fun access(): FolderAccess

    /** Открывает выбор папки. Подходит `Black Desert` или `Документы`, в которой она лежит. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Повторно запрашивает разрешение на выбранную раньше папку. Вызывать только по клику. */
    suspend fun requestPermission(): FolderAccess.Granted

    /** ID персонажей, для которых есть `FaceTexture\<ID>.bmp`. */
    suspend fun portraitIds(): List<String>

    suspend fun readPortrait(characterId: String): ByteArray

    /** Создаёт пустую папку `FaceTexture_backup\<дата-время>` и возвращает её имя. */
    suspend fun createBackup(): String

    suspend fun writePortrait(characterId: String, bmp: ByteArray)

    /** Список папок `FaceTexture_backup` в формате [com.layzen.bdotools.feature.portraitconstructor.domain.parseBackupList]. */
    suspend fun backupListing(): String

    /** Файл из резервной копии [backup]; `null`, если файла нет. */
    suspend fun readBackupFile(backup: String, name: String): ByteArray?

    suspend fun writeBackupFile(backup: String, name: String, bytes: ByteArray)

    /** Удаляет папку `FaceTexture_backup\<backup>` целиком. */
    suspend fun deleteBackup(backup: String)

    /** Файлы в `Black Desert\BDO Tools\portraits`: чистые портреты и украшения. */
    suspend fun toolFileNames(): List<String>

    /** `null`, если файла нет. */
    suspend fun readToolFile(name: String): ByteArray?

    suspend fun writeToolFile(name: String, bytes: ByteArray)

    suspend fun deleteToolFile(name: String)

    /** Создаёт пустую папку `Favorite FaceTexture\<дата-время>` и возвращает её имя. */
    suspend fun createFavoriteSet(): String

    /** Список папок `Favorite FaceTexture` в том же формате, что [backupListing]. */
    suspend fun favoriteListing(): String

    /** Файл из папки избранного [set]; `null`, если файла нет. */
    suspend fun readFavoriteFile(set: String, name: String): ByteArray?

    suspend fun writeFavoriteFile(set: String, name: String, bytes: ByteArray)

    /** Удаляет папку `Favorite FaceTexture\<set>` целиком. */
    suspend fun deleteFavoriteSet(set: String)

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
