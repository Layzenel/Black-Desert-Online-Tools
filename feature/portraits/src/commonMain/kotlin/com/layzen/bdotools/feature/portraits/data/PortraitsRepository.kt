package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.feature.portraits.domain.BackupInfo
import com.layzen.bdotools.feature.portraits.domain.FavoriteSet
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.decodeDecoration
import com.layzen.bdotools.feature.portraits.domain.encodeDecoration
import com.layzen.bdotools.feature.portraits.domain.parseBackupList
import com.layzen.bdotools.feature.portraits.domain.sortByCharacterOrder

private const val BMP_EXTENSION = ".bmp"
private const val DECORATION_EXTENSION = ".txt"
// не .bmp, чтобы список портретов набора не принял чистую основу за портрет
private const val FAVORITE_BASE_EXTENSION = ".base"
/** Название набора избранного, которое задал пользователь. */
private const val FAVORITE_TITLE_FILE = "name.txt"

/** Порядок портретов как в окне выбора персонажа и запись новых портретов с резервной копией. */
class PortraitsRepository(private val storage: PortraitsStorage) {

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
    suspend fun backups(): List<BackupInfo> = parseBackupList(storage.backupListing())

    suspend fun readBackupPortrait(backup: String, characterId: String): ByteArray =
        storage.readBackupPortrait(backup, characterId)

    /** Удаляет копии по одной и возвращает обновлённый список. */
    suspend fun deleteBackups(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<BackupInfo> {
        names.forEachIndexed { index, name ->
            storage.deleteBackup(name)
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

    /** Наборы в `Favorite FaceTexture` с названиями, от новых к старым. */
    suspend fun favoriteSets(): List<FavoriteSet> =
        parseBackupList(storage.favoriteListing()).map { set ->
            val title = storage.readFavoriteFile(set.name, FAVORITE_TITLE_FILE)?.decodeToString()
            FavoriteSet(set.name, set.characterIds, title?.let(FavoriteSet::normalizeTitle))
        }

    /** Меняет название набора; пустое название возвращает подпись датой. */
    suspend fun renameFavoriteSet(set: String, title: String) {
        val normalized = FavoriteSet.normalizeTitle(title).orEmpty()
        storage.writeFavoriteFile(set, FAVORITE_TITLE_FILE, normalized.encodeToByteArray())
    }

    /**
     * Заменяет портреты набора [set] текущими: сохраняет новый набор с тем же названием, затем удаляет старый.
     * Новый набор пишется первым, поэтому сбой посередине не оставит пользователя без обоих. Возвращает имя нового.
     */
    suspend fun replaceFavoriteSet(
        set: String,
        characterIds: List<String>,
        decorations: Map<String, PortraitDecoration>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val title = storage.readFavoriteFile(set, FAVORITE_TITLE_FILE)
        val replacement = saveFavoriteSet(characterIds, decorations, onProgress)
        if (title != null) storage.writeFavoriteFile(replacement, FAVORITE_TITLE_FILE, title)
        storage.deleteFavoriteSet(set)
        return replacement
    }

    /** Удаляет наборы по одному и возвращает обновлённый список. */
    suspend fun deleteFavoriteSets(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<FavoriteSet> {
        names.forEachIndexed { index, name ->
            storage.deleteFavoriteSet(name)
            onProgress(index + 1, names.size)
        }
        return favoriteSets()
    }

    suspend fun readFavoritePortrait(set: String, characterId: String): ByteArray =
        storage.readFavoriteFile(set, characterId + BMP_EXTENSION) ?: throw PortraitsException(PortraitsError.Unknown)

    /**
     * Сохраняет портреты персонажей [ids] (всех на странице) в новый набор избранного. У портретов с надписью или
     * значками рядом лежат чистая основа и украшение, чтобы после применения их можно было менять. Возвращает имя набора.
     */
    suspend fun saveFavoriteSet(
        ids: List<String>,
        decorations: Map<String, PortraitDecoration>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val set = storage.createFavoriteSet()
        ids.forEachIndexed { index, id ->
            storage.writeFavoriteFile(set, id + BMP_EXTENSION, storage.readPortrait(id))
            val decoration = decorations[id]
            if (decoration != null && !decoration.isEmpty) {
                storage.writeFavoriteFile(set, id + FAVORITE_BASE_EXTENSION, readCleanBase(id))
                storage.writeFavoriteFile(set, id + DECORATION_EXTENSION, encodeDecoration(decoration).encodeToByteArray())
            }
            onProgress(index + 1, ids.size)
        }
        return set
    }

    /** Что записать персонажу, чтобы вернуть ему портрет из набора [set] вместе с надписью и значками. */
    suspend fun favoriteWrite(set: String, characterId: String): PortraitWrite {
        val face = readFavoritePortrait(set, characterId)
        val decoration = storage.readFavoriteFile(set, characterId + DECORATION_EXTENSION)
            ?.let { decodeDecoration(it.decodeToString()) }
            ?: PortraitDecoration()
        val base = storage.readFavoriteFile(set, characterId + FAVORITE_BASE_EXTENSION) ?: face
        return PortraitWrite(characterId, face, base, decoration)
    }

    /**
     * Что записать, чтобы персонажи [first] и [second] обменялись портретами — вместе с чистой основой, надписью
     * и значками. Нужно, когда в игре персонажей переставили и нарезанная на портреты картинка разъехалась.
     */
    suspend fun swapWrites(first: String, second: String, decorations: Map<String, PortraitDecoration>): List<PortraitWrite> {
        val a = currentWrite(first, decorations)
        val b = currentWrite(second, decorations)
        return listOf(
            PortraitWrite(first, b.face, b.cleanBase, b.decoration),
            PortraitWrite(second, a.face, a.cleanBase, a.decoration),
        )
    }

    private suspend fun currentWrite(characterId: String, decorations: Map<String, PortraitDecoration>) = PortraitWrite(
        characterId,
        face = storage.readPortrait(characterId),
        cleanBase = readCleanBase(characterId),
        decoration = decorations[characterId] ?: PortraitDecoration(),
    )

    /** Сохраняет копию старых файлов `FaceTexture` и записывает новые. Возвращает путь к резервной копии. */
    suspend fun writePortraits(
        writes: List<PortraitWrite>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val backup = storage.backupPortraits(writes.map { it.characterId })
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
}
