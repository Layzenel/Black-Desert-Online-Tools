package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.feature.portraitconstructor.domain.BackupInfo
import com.layzen.bdotools.feature.portraitconstructor.domain.FavoriteSet
import com.layzen.bdotools.feature.portraitconstructor.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.containing
import com.layzen.bdotools.feature.portraitconstructor.domain.decodeDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.encodeDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.parseBackupList
import com.layzen.bdotools.feature.portraitconstructor.domain.sortByCharacterOrder

private const val BMP_EXTENSION = ".bmp"
private const val DECORATION_EXTENSION = ".txt"
private const val BACKUP_FOLDER = "FaceTexture_backup"
private const val PATH_SEPARATOR = '\\'
// в наборах прошлых версий .bmp — портрет с украшениями, а чистая основа лежит в этом файле
private const val LEGACY_FAVORITE_BASE_EXTENSION = ".base"
/** Название набора избранного, которое задал пользователь. */
private const val FAVORITE_TITLE_FILE = "name.txt"

/**
 * Порядок портретов как в окне выбора персонажа и запись новых портретов с резервной копией.
 * Избранное и копии хранят портреты как [StoredPortrait]: чистая основа и украшение отдельно.
 */
class PortraitsRepository(private val storage: PortraitsStorage) {

    /** Отпечатки портретов в копиях: `<копия>/<ID>` → [StoredPortrait.fingerprint]. Копии после записи не меняются. */
    private val backupFingerprints = mutableMapOf<String, Int>()

    /** Аккаунт, в чьих папках лежат копии и избранное (см. [PortraitsStorage]); `null` — без аккаунта. */
    private var account: String? = null

    /**
     * Переключает копии и избранное на папки аккаунта [id]. Копии и наборы из версий до аккаунтов, лежащие без папки
     * аккаунта, переносятся в папку того аккаунта, который открыли первым.
     */
    suspend fun useAccount(id: String?) {
        if (id != null) storage.adoptLooseFolders(id)
        account = id
        backupFingerprints.clear()
    }

    /** Путь набора избранного [set] от `Favorite FaceTexture` — для сообщений. */
    fun favoritePath(set: String): String = account?.let { it + PATH_SEPARATOR + set } ?: set

    /**
     * ID персонажей [account] в порядке окна выбора персонажа: портреты других аккаунтов в `FaceTexture`
     * пропускаются. Без аккаунта (в `UserCache` нет списка персонажей) — все портреты по ID.
     * Сами портреты читаются по одному через [readPortrait].
     */
    suspend fun portraitOrder(account: GameAccount?): List<String> {
        val ids = storage.portraitIds()
        if (account == null) return sortByCharacterOrder(ids, emptyMap())
        return sortByCharacterOrder(ids.filter { it in account.characterOrder }, account.characterOrder)
    }

    suspend fun readPortrait(characterId: String): ByteArray = storage.readPortrait(characterId)

    /** Резервные копии от новых к старым. */
    suspend fun backups(): List<BackupInfo> = parseBackupList(storage.backupListing(account))

    /** Портрет из копии. В копиях прошлых версий лежит только готовый портрет — он читается без украшения. */
    suspend fun readBackupPortrait(backup: String, characterId: String): StoredPortrait =
        StoredPortrait.read(characterId) { storage.readBackupFile(account, backup, it) }

    /** Удаляет копии по одной и возвращает обновлённый список. */
    suspend fun deleteBackups(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<BackupInfo> {
        names.forEachIndexed { index, name ->
            storage.deleteBackup(account, name)
            onProgress(index + 1, names.size)
        }
        return backups()
    }

    suspend fun loadDecorations(): Map<String, PortraitDecoration> =
        storage.toolFileNames()
            .filter { it.endsWith(DECORATION_EXTENSION) }
            .mapNotNull { name ->
                val text = storage.readToolFile(name)?.decodeToString() ?: return@mapNotNull null
                name.removeSuffix(DECORATION_EXTENSION) to decodeDecoration(text)
            }
            .filterNot { it.second.isEmpty }
            .toMap()

    /** Портрет без украшений: сохранённая основа, а если её нет — текущий файл из `FaceTexture`. */
    suspend fun readCleanBase(characterId: String): ByteArray =
        storage.readToolFile(characterId + BMP_EXTENSION) ?: storage.readPortrait(characterId)

    /** Текущий портрет персонажа: чистая основа и украшение из `BDO Tools\portraits`. */
    suspend fun currentPortrait(characterId: String): StoredPortrait {
        val decoration = storage.readToolFile(characterId + DECORATION_EXTENSION)?.let { decodeDecoration(it.decodeToString()) }
        return StoredPortrait(readCleanBase(characterId), decoration ?: PortraitDecoration())
    }

    /** Наборы в `Favorite FaceTexture` с названиями, от новых к старым. */
    suspend fun favoriteSets(): List<FavoriteSet> =
        parseBackupList(storage.favoriteListing(account)).map { set ->
            val title = storage.readFavoriteFile(account, set.name, FAVORITE_TITLE_FILE)?.decodeToString()
            FavoriteSet(set.name, set.characterIds, title?.let(FavoriteSet::normalizeTitle))
        }

    /** Меняет название набора; пустое название возвращает подпись датой. */
    suspend fun renameFavoriteSet(set: String, title: String) {
        val normalized = FavoriteSet.normalizeTitle(title).orEmpty()
        storage.writeFavoriteFile(account, set, FAVORITE_TITLE_FILE, normalized.encodeToByteArray())
    }

    /**
     * Заменяет портреты набора [set] текущими: сохраняет новый набор с тем же названием, затем удаляет старый.
     * Новый набор пишется первым, поэтому сбой посередине не оставит пользователя без обоих. Возвращает имя нового.
     */
    suspend fun replaceFavoriteSet(
        set: String,
        characterIds: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val title = storage.readFavoriteFile(account, set, FAVORITE_TITLE_FILE)
        val replacement = saveFavoriteSet(characterIds, onProgress)
        if (title != null) storage.writeFavoriteFile(account, replacement, FAVORITE_TITLE_FILE, title)
        storage.deleteFavoriteSet(account, set)
        return replacement
    }

    /** Удаляет наборы по одному и возвращает обновлённый список. */
    suspend fun deleteFavoriteSets(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<FavoriteSet> {
        names.forEachIndexed { index, name ->
            storage.deleteFavoriteSet(account, name)
            onProgress(index + 1, names.size)
        }
        return favoriteSets()
    }

    /** Портрет из набора [set]. В наборах прошлых версий чистая основа лежала отдельно от готового портрета. */
    suspend fun readFavoritePortrait(set: String, characterId: String): StoredPortrait {
        val stored = StoredPortrait.read(characterId) { storage.readFavoriteFile(account, set, it) }
        val legacyBase = storage.readFavoriteFile(account, set, characterId + LEGACY_FAVORITE_BASE_EXTENSION) ?: return stored
        return StoredPortrait(legacyBase, stored.decoration)
    }

    /** Сохраняет текущие портреты персонажей [ids] (всех на странице) в новый набор избранного. Возвращает имя набора. */
    suspend fun saveFavoriteSet(
        ids: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val set = storage.createFavoriteSet(account)
        ids.forEachIndexed { index, id ->
            currentPortrait(id).writeTo(id) { name, bytes -> storage.writeFavoriteFile(account, set, name, bytes) }
            onProgress(index + 1, ids.size)
        }
        return set
    }

    /** Сохраняет текущие портреты в резервную копию и записывает новые. Возвращает путь к копии. */
    suspend fun writePortraits(
        writes: List<PortraitWrite>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val backup = backupCurrent(writes.map { it.characterId })
        writes.forEachIndexed { index, write ->
            val id = write.characterId
            if (write.decoration.isEmpty) {
                storage.deleteToolFile(id + BMP_EXTENSION)
                storage.deleteToolFile(id + DECORATION_EXTENSION)
            } else {
                storage.writeToolFile(id + BMP_EXTENSION, write.cleanBase)
                storage.writeToolFile(id + DECORATION_EXTENSION, encodeDecoration(write.decoration).encodeToByteArray())
            }
            storage.writePortrait(id, write.face)
            onProgress(index + 1, writes.size)
        }
        return backup
    }

    /**
     * Сохраняет текущие портреты [ids] в новую копию, кроме тех, что уже лежат в какой-то копии без изменений.
     * Папка создаётся, только если есть что в неё положить. Возвращает путь к новой копии, а если новых портретов
     * нет — к копии, где уже лежит первый из них.
     */
    private suspend fun backupCurrent(ids: List<String>): String {
        val existing = backups()
        var created: String? = null
        var duplicateIn: String? = null
        for (id in ids) {
            val portrait = currentPortrait(id)
            val copy = findBackupCopy(id, portrait, existing)
            if (copy != null) {
                duplicateIn = duplicateIn ?: copy
                continue
            }
            val folder = created ?: storage.createBackup(account).also { created = it }
            portrait.writeTo(id) { name, bytes -> storage.writeBackupFile(account, folder, name, bytes) }
            backupFingerprints["$folder/$id"] = portrait.fingerprint
        }
        val folder = created ?: duplicateIn.orEmpty()
        return listOfNotNull(BACKUP_FOLDER, account, folder).joinToString(PATH_SEPARATOR.toString())
    }

    /** Копия из [backups], где уже лежит такой же [portrait] персонажа, или `null`. */
    private suspend fun findBackupCopy(id: String, portrait: StoredPortrait, backups: List<BackupInfo>): String? =
        backups.containing(id).firstOrNull { backup ->
            val key = "${backup.name}/$id"
            val known = backupFingerprints[key]
            if (known != null && known != portrait.fingerprint) return@firstOrNull false
            val stored = readBackupPortrait(backup.name, id)
            backupFingerprints[key] = stored.fingerprint
            stored.sameAs(portrait)
        }?.name
}
