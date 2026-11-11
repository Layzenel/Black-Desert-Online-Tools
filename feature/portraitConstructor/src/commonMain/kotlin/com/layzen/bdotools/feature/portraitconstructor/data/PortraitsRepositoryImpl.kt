package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.BackupInfo
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.containing
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.parseBackupList
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.decodeDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.encodeDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.favorite.FavoriteSet
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitWrite
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsRepository
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.StoredPortrait
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.sortByCharacterOrder

private const val BMP_EXTENSION = ".bmp"
private const val DECORATION_EXTENSION = ".txt"
/** Папка тулзы от `Black Desert` — для путей в сообщениях о записи. */
private const val TOOL_FOLDER = "BDO Tools\\portraitConstructor"
private const val BACKUP_FOLDER = "FaceTexture_backup"
private const val FAVORITE_FOLDER = "Favorite FaceTexture"
private const val PATH_SEPARATOR = '\\'
// в наборах прошлых версий .bmp — портрет с украшениями, а чистая основа лежит в этом файле
private const val LEGACY_FAVORITE_BASE_EXTENSION = ".base"
/** Название набора избранного, которое задал пользователь. */
private const val FAVORITE_TITLE_FILE = "name.txt"

/**
 * Порядок портретов как в окне выбора персонажа и запись новых портретов с резервной копией.
 * Избранное и копии хранят портреты как [StoredPortrait]: чистая основа и украшение отдельно.
 */
class PortraitsRepositoryImpl(private val storage: PortraitsStorage) : PortraitsRepository {

    /** Отпечатки портретов в копиях: `<копия>/<ID>` → [StoredPortrait.fingerprint]. Копии после записи не меняются. */
    private val backupFingerprints = mutableMapOf<String, Int>()

    /** Аккаунт, в чьей папке лежат копии, избранное и текущие портреты (см. [PortraitsStorage]); `null` — без аккаунта. */
    private var account: String? = null

    override suspend fun useAccount(selected: GameAccount?) {
        if (selected != null) storage.adoptLooseFolders(selected.id, selected.characterOrder.keys)
        account = selected?.id
        backupFingerprints.clear()
    }

    override fun favoritePath(set: String): String = listOfNotNull(account, FAVORITE_FOLDER, set).joinToString(PATH_SEPARATOR.toString())

    override suspend fun portraitOrder(account: GameAccount?): List<String> {
        val ids = storage.portraitIds()
        if (account == null) return sortByCharacterOrder(ids, emptyMap())
        return sortByCharacterOrder(ids.filter { it in account.characterOrder }, account.characterOrder)
    }

    override suspend fun readPortrait(characterId: String): ByteArray = storage.readPortrait(characterId)

    override suspend fun backups(): List<BackupInfo> = parseBackupList(storage.backupListing(account))

    override suspend fun readBackupPortrait(backup: String, characterId: String): StoredPortrait =
        StoredPortrait.read(characterId) { storage.readBackupFile(account, backup, it) }

    override suspend fun deleteBackups(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit,
    ): List<BackupInfo> {
        names.forEachIndexed { index, name ->
            storage.deleteBackup(account, name)
            onProgress(index + 1, names.size)
        }
        return backups()
    }

    override suspend fun loadDecorations(): Map<String, PortraitDecoration> =
        storage.toolFileNames(account)
            .filter { it.endsWith(DECORATION_EXTENSION) }
            .mapNotNull { name ->
                val text = storage.readToolFile(account, name)?.decodeToString() ?: return@mapNotNull null
                name.removeSuffix(DECORATION_EXTENSION) to decodeDecoration(text)
            }
            .filterNot { it.second.isEmpty }
            .toMap()

    override suspend fun readCleanBase(characterId: String): ByteArray =
        storage.readToolFile(account, characterId + BMP_EXTENSION) ?: storage.readPortrait(characterId)

    override suspend fun currentPortrait(characterId: String): StoredPortrait {
        val decoration = storage.readToolFile(account, characterId + DECORATION_EXTENSION)?.let { decodeDecoration(it.decodeToString()) }
        return StoredPortrait(readCleanBase(characterId), decoration ?: PortraitDecoration())
    }

    override suspend fun favoriteSets(): List<FavoriteSet> =
        parseBackupList(storage.favoriteListing(account)).map { set ->
            val title = storage.readFavoriteFile(account, set.name, FAVORITE_TITLE_FILE)?.decodeToString()
            FavoriteSet(set.name, set.characterIds, title?.let(FavoriteSet::normalizeTitle))
        }

    override suspend fun renameFavoriteSet(set: String, title: String) {
        val normalized = FavoriteSet.normalizeTitle(title).orEmpty()
        storage.writeFavoriteFile(account, set, FAVORITE_TITLE_FILE, normalized.encodeToByteArray())
    }

    override suspend fun replaceFavoriteSet(
        set: String,
        characterIds: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit,
    ): String {
        val title = storage.readFavoriteFile(account, set, FAVORITE_TITLE_FILE)
        val replacement = saveFavoriteSet(characterIds, onProgress)
        if (title != null) storage.writeFavoriteFile(account, replacement, FAVORITE_TITLE_FILE, title)
        storage.deleteFavoriteSet(account, set)
        return replacement
    }

    override suspend fun deleteFavoriteSets(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit,
    ): List<FavoriteSet> {
        names.forEachIndexed { index, name ->
            storage.deleteFavoriteSet(account, name)
            onProgress(index + 1, names.size)
        }
        return favoriteSets()
    }

    override suspend fun readFavoritePortrait(set: String, characterId: String): StoredPortrait {
        val stored = StoredPortrait.read(characterId) { storage.readFavoriteFile(account, set, it) }
        val legacyBase = storage.readFavoriteFile(account, set, characterId + LEGACY_FAVORITE_BASE_EXTENSION) ?: return stored
        return StoredPortrait(legacyBase, stored.decoration)
    }

    override suspend fun saveFavoriteSet(
        ids: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit,
    ): String {
        val set = storage.createFavoriteSet(account)
        ids.forEachIndexed { index, id ->
            currentPortrait(id).writeTo(id) { name, bytes -> storage.writeFavoriteFile(account, set, name, bytes) }
            onProgress(index + 1, ids.size)
        }
        return set
    }

    override suspend fun writePortraits(
        writes: List<PortraitWrite>,
        onProgress: suspend (done: Int, total: Int) -> Unit,
    ): String {
        val backup = backupCurrent(writes.map { it.characterId })
        writes.forEachIndexed { index, write ->
            val id = write.characterId
            if (write.decoration.isEmpty) {
                storage.deleteToolFile(account, id + BMP_EXTENSION)
                storage.deleteToolFile(account, id + DECORATION_EXTENSION)
            } else {
                storage.writeToolFile(account, id + BMP_EXTENSION, write.cleanBase)
                storage.writeToolFile(account, id + DECORATION_EXTENSION, encodeDecoration(write.decoration).encodeToByteArray())
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
        return listOfNotNull(TOOL_FOLDER, account, BACKUP_FOLDER, folder).joinToString(PATH_SEPARATOR.toString())
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

    override suspend fun access(): FolderAccess = storage.access()

    override suspend fun selectFolder(): FolderAccess.Granted = storage.selectFolder()

    override suspend fun requestPermission(): FolderAccess.Granted = storage.requestPermission()
}
