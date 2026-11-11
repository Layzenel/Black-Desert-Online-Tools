package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration

/**
 * Сценарии страницы портретов: доступ к папке игры, чтение портретов выбранного аккаунта, новое изображение
 * и запись портретов. Копии, избранное и заметки — у своих интеракторов.
 */
class PortraitsInteractor(
    private val repository: PortraitsRepository,
    private val imageSource: ImageSource,
) {

    /** См. [PortraitsRepository.access]. */
    suspend fun access(): FolderAccess = repository.access()

    /** См. [PortraitsRepository.selectFolder]. */
    suspend fun selectFolder(): FolderAccess.Granted = repository.selectFolder()

    /** См. [PortraitsRepository.requestPermission]. */
    suspend fun requestPermission(): FolderAccess.Granted = repository.requestPermission()

    /** См. [PortraitsRepository.useAccount]. */
    suspend fun useAccount(selected: GameAccount?) = repository.useAccount(selected)

    /** См. [PortraitsRepository.portraitOrder]. */
    suspend fun portraitOrder(account: GameAccount?): List<String> = repository.portraitOrder(account)

    /** См. [PortraitsRepository.readPortrait]. */
    suspend fun readPortrait(characterId: String): ByteArray = repository.readPortrait(characterId)

    /** См. [PortraitsRepository.readCleanBase]. */
    suspend fun readCleanBase(characterId: String): ByteArray = repository.readCleanBase(characterId)

    /** См. [PortraitsRepository.currentPortrait]. */
    suspend fun currentPortrait(characterId: String): StoredPortrait = repository.currentPortrait(characterId)

    /** См. [PortraitsRepository.loadDecorations]. */
    suspend fun loadDecorations(): Map<String, PortraitDecoration> = repository.loadDecorations()

    /** См. [PortraitsRepository.writePortraits]. */
    suspend fun writePortraits(
        writes: List<PortraitWrite>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String = repository.writePortraits(writes, onProgress)

    /** См. [ImageSource.pickFromDevice]. */
    suspend fun pickImage(): ByteArray? = imageSource.pickFromDevice()

    /** См. [ImageSource.download]. */
    suspend fun downloadImage(url: String): ByteArray = imageSource.download(url)
}
