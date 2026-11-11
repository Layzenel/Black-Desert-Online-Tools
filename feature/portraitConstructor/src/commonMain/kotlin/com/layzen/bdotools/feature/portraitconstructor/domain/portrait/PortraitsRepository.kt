package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.BackupInfo
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.favorite.FavoriteSet

/**
 * Портреты персонажей в папке `Документы\Black Desert`: текущие, резервные копии и избранное.
 * Избранное и копии хранят портреты как [StoredPortrait]: чистая основа и украшение отдельно.
 * Ошибки работы с файлами — [PortraitsException].
 */
interface PortraitsRepository {

    /** Есть ли доступ к папке игры; ничего не спрашивает у пользователя. */
    suspend fun access(): FolderAccess

    /** Просит пользователя выбрать папку `Black Desert` и запоминает её. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Просит разрешение на уже выбранную папку (браузер спрашивает его заново после перезагрузки страницы). */
    suspend fun requestPermission(): FolderAccess.Granted

    /**
     * Переключает копии, избранное и текущие портреты на папку аккаунта [selected]. Данные прошлых версий
     * переносятся в неё: копии и наборы без аккаунта — в аккаунт, который открыли первым, чистые портреты
     * и украшения — в аккаунт их персонажей.
     */
    suspend fun useAccount(selected: GameAccount?)

    /**
     * ID персонажей [account] в порядке окна выбора персонажа: портреты других аккаунтов в `FaceTexture`
     * пропускаются. Без аккаунта (в `UserCache` нет списка персонажей) — все портреты по ID.
     * Сами портреты читаются по одному через [readPortrait].
     */
    suspend fun portraitOrder(account: GameAccount?): List<String>

    /** Байты текущего портрета персонажа [characterId] из `FaceTexture`. */
    suspend fun readPortrait(characterId: String): ByteArray

    /** Портрет без украшений: сохранённая основа, а если её нет — текущий файл из `FaceTexture`. */
    suspend fun readCleanBase(characterId: String): ByteArray

    /** Текущий портрет персонажа: чистая основа и украшение из `Current portraits` аккаунта. */
    suspend fun currentPortrait(characterId: String): StoredPortrait

    /** Украшения, сохранённые для портретов выбранного аккаунта, по ID персонажа; пустые пропускаются. */
    suspend fun loadDecorations(): Map<String, PortraitDecoration>

    /** Сохраняет текущие портреты в резервную копию и записывает новые. Возвращает путь к копии. */
    suspend fun writePortraits(
        writes: List<PortraitWrite>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String

    /** Резервные копии от новых к старым. */
    suspend fun backups(): List<BackupInfo>

    /** Портрет из копии. В копиях прошлых версий лежит только готовый портрет — он читается без украшения. */
    suspend fun readBackupPortrait(backup: String, characterId: String): StoredPortrait

    /** Удаляет копии по одной и возвращает обновлённый список. */
    suspend fun deleteBackups(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<BackupInfo>

    /** Наборы в `Favorite FaceTexture` с названиями, от новых к старым. */
    suspend fun favoriteSets(): List<FavoriteSet>

    /** Путь набора избранного [set] от `BDO Tools\portraitConstructor` — для сообщений. */
    fun favoritePath(set: String): String

    /** Портрет из набора [set]. В наборах прошлых версий чистая основа лежала отдельно от готового портрета. */
    suspend fun readFavoritePortrait(set: String, characterId: String): StoredPortrait

    /** Сохраняет текущие портреты персонажей [ids] (всех на странице) в новый набор избранного. Возвращает имя набора. */
    suspend fun saveFavoriteSet(
        ids: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String

    /** Меняет название набора; пустое название возвращает подпись датой. */
    suspend fun renameFavoriteSet(set: String, title: String)

    /**
     * Заменяет портреты набора [set] текущими: сохраняет новый набор с тем же названием, затем удаляет старый.
     * Новый набор пишется первым, поэтому сбой посередине не оставит пользователя без обоих. Возвращает имя нового.
     */
    suspend fun replaceFavoriteSet(
        set: String,
        characterIds: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String

    /** Удаляет наборы по одному и возвращает обновлённый список. */
    suspend fun deleteFavoriteSets(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<FavoriteSet>
}
