package com.layzen.bdotools.feature.portraitconstructor.domain.backup

import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsRepository
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.StoredPortrait

/** Сценарии резервных копий портретов: список, чтение портрета из копии и удаление. */
class BackupsInteractor(private val repository: PortraitsRepository) {

    /** См. [PortraitsRepository.backups]. */
    suspend fun backups(): List<BackupInfo> = repository.backups()

    /** См. [PortraitsRepository.readBackupPortrait]. */
    suspend fun readPortrait(backup: String, characterId: String): StoredPortrait =
        repository.readBackupPortrait(backup, characterId)

    /** См. [PortraitsRepository.deleteBackups]. */
    suspend fun delete(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<BackupInfo> = repository.deleteBackups(names, onProgress)
}
