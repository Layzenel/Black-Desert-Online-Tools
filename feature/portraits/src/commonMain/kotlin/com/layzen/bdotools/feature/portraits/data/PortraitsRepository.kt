package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.feature.portraits.domain.BackupInfo
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.decodeDecoration
import com.layzen.bdotools.feature.portraits.domain.encodeDecoration
import com.layzen.bdotools.feature.portraits.domain.parseBackupList
import com.layzen.bdotools.feature.portraits.domain.pickCharacterOrder
import com.layzen.bdotools.feature.portraits.domain.sortByCharacterOrder

private const val BASE_EXTENSION = ".bmp"
private const val DECORATION_EXTENSION = ".txt"

/** Порядок портретов как в окне выбора персонажа и запись новых портретов с резервной копией. */
class PortraitsRepository(private val storage: PortraitsStorage) {

    /** ID персонажей в порядке окна выбора персонажа. Сами портреты читаются по одному через [readPortrait]. */
    suspend fun portraitOrder(): List<String> {
        val ids = storage.portraitIds()
        val order = pickCharacterOrder(storage.characterOrderCandidates(), ids)
        return sortByCharacterOrder(ids, order)
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
        storage.readToolFile(characterId + BASE_EXTENSION) ?: storage.readPortrait(characterId)

    /** Сохраняет копию старых файлов `FaceTexture` и записывает новые. Возвращает путь к резервной копии. */
    suspend fun writePortraits(
        writes: List<PortraitWrite>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String {
        val backup = storage.backupPortraits(writes.map { it.characterId })
        writes.forEachIndexed { index, write ->
            val id = write.characterId
            if (write.decoration.isEmpty) {
                storage.deleteToolFile(id + BASE_EXTENSION)
                storage.deleteToolFile(id + DECORATION_EXTENSION)
            } else {
                storage.writeToolFile(id + BASE_EXTENSION, write.cleanBase)
                storage.writeToolFile(id + DECORATION_EXTENSION, encodeDecoration(write.decoration).encodeToByteArray())
            }
            storage.writePortrait(id, write.face)
            onProgress(index + 1, writes.size)
        }
        return backup
    }
}
