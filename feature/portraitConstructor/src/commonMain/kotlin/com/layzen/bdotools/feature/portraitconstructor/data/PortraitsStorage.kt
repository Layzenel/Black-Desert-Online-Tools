package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess

/**
 * Файлы игры в папке `Документы\Black Desert`. Все методы, кроме [access], требуют [FolderAccess.Granted].
 * Своё тулза хранит в папке аккаунта `BDO Tools\portraitConstructor\<account>\`: `FaceTexture_backup` (резервные
 * копии), `Favorite FaceTexture` (избранное), `Current portraits` (чистые портреты и украшения) и `lobby_backup`
 * (копии файла заметок). `account = null` — аккаунтов нет, папки прямо в `BDO Tools\portraitConstructor`.
 */
interface PortraitsStorage {
    /** Состояние доступа к папке `Black Desert`; единственный метод, который можно звать без [FolderAccess.Granted]. */
    suspend fun access(): FolderAccess

    /** Открывает выбор папки. Подходит `Black Desert` или `Документы`, в которой она лежит. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Повторно запрашивает разрешение на выбранную раньше папку. Вызывать только по клику. */
    suspend fun requestPermission(): FolderAccess.Granted

    /** ID персонажей, для которых есть `FaceTexture\<ID>.bmp`. */
    suspend fun portraitIds(): List<String>

    /** Байты `FaceTexture\<characterId>.bmp`. */
    suspend fun readPortrait(characterId: String): ByteArray

    /** Создаёт пустую папку `<account>\FaceTexture_backup\<дата-время>` и возвращает её имя. */
    suspend fun createBackup(account: String?): String

    /** Перезаписывает `FaceTexture\<characterId>.bmp` байтами [bmp]. */
    suspend fun writePortrait(characterId: String, bmp: ByteArray)

    /** Список папок `<account>\FaceTexture_backup` в формате [com.layzen.bdotools.feature.portraitconstructor.domain.parseBackupList]. */
    suspend fun backupListing(account: String?): String

    /** Файл из резервной копии [backup]; `null`, если файла нет. */
    suspend fun readBackupFile(account: String?, backup: String, name: String): ByteArray?

    /** Пишет файл [name] в резервную копию [backup]. */
    suspend fun writeBackupFile(account: String?, backup: String, name: String, bytes: ByteArray)

    /** Удаляет папку `<account>\FaceTexture_backup\<backup>` целиком. */
    suspend fun deleteBackup(account: String?, backup: String)

    /** Файлы в `<account>\Current portraits`: чистые портреты и украшения. */
    suspend fun toolFileNames(account: String?): List<String>

    /** `null`, если файла нет. */
    suspend fun readToolFile(account: String?, name: String): ByteArray?

    /** Пишет файл [name] в `<account>\Current portraits`. */
    suspend fun writeToolFile(account: String?, name: String, bytes: ByteArray)

    /** Удаляет файл [name] из `<account>\Current portraits`. */
    suspend fun deleteToolFile(account: String?, name: String)

    /** Создаёт пустую папку `<account>\Favorite FaceTexture\<дата-время>` и возвращает её имя. */
    suspend fun createFavoriteSet(account: String?): String

    /** Список папок `<account>\Favorite FaceTexture` в том же формате, что [backupListing]. */
    suspend fun favoriteListing(account: String?): String

    /** Файл из папки избранного [set]; `null`, если файла нет. */
    suspend fun readFavoriteFile(account: String?, set: String, name: String): ByteArray?

    /** Пишет файл [name] в папку избранного [set]. */
    suspend fun writeFavoriteFile(account: String?, set: String, name: String, bytes: ByteArray)

    /** Удаляет папку `<account>\Favorite FaceTexture\<set>` целиком. */
    suspend fun deleteFavoriteSet(account: String?, set: String)

    /**
     * Переносит данные прошлых версий в папку аккаунта (совпавшее имя получает суффикс, ничего не перезаписывается):
     * - копии и избранное из корня `Black Desert` и из `BDO Tools\portraits\<FaceTexture_backup|Favorite FaceTexture>`:
     *   папки с номером аккаунта — в папку своего аккаунта, остальные (имя — дата) — в [account];
     * - чистые портреты и украшения из `BDO Tools\portraits` — только персонажей [characterIds], в `Current portraits`;
     * - копии файла заметок из `BDO Tools\lobby_backup\<аккаунт>` — в папку своего аккаунта.
     * Опустевшие старые папки удаляются.
     */
    suspend fun adoptLooseFolders(account: String, characterIds: Set<String>)

    /** `UserCache\<account>\gameVariable_lobby.xml`; `null`, если файла нет. */
    suspend fun readLobbyVariables(account: String): ByteArray?

    /** Перезаписывает `UserCache\<account>\gameVariable_lobby.xml` байтами [bytes]. */
    suspend fun writeLobbyVariables(account: String, bytes: ByteArray)

    /** Сохраняет [bytes] в `<account>\lobby_backup\<дата-время>.xml` и возвращает путь к копии от `Black Desert`. */
    suspend fun backupLobbyVariables(account: String, bytes: ByteArray): String

    /**
     * Запущена ли игра: пока она работает, её файлы настроек перезапишутся данными из её памяти при смене персонажа, телепорте или выходе.
     * `null` — платформа проверить не может (браузер не видит процессы).
     */
    suspend fun isGameRunning(): Boolean?
}
