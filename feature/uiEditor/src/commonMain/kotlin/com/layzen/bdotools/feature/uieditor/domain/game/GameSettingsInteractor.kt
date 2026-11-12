package com.layzen.bdotools.feature.uieditor.domain.game

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.file.DeviceFiles
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout

/**
 * Сценарии работы с настройками игры: доступ к папке, чтение `gamevariable.xml` аккаунта, запись раскладки
 * и подложка редактора. Пресеты и файлы раскладок — у своих интеракторов.
 */
class GameSettingsInteractor(
    private val repository: SlotsRepository,
    private val device: DeviceFiles,
) {

    /** См. [SlotsRepository.access]. */
    suspend fun access(): FolderAccess = repository.access()

    /** См. [SlotsRepository.selectFolder]. */
    suspend fun selectFolder(): FolderAccess.Granted = repository.selectFolder()

    /** См. [SlotsRepository.requestPermission]. */
    suspend fun requestPermission(): FolderAccess.Granted = repository.requestPermission()

    /** См. [SlotsRepository.accounts]. */
    suspend fun accounts(): List<String> = repository.accounts()

    /**
     * Читает файл аккаунта (см. [SlotsRepository.load]) и перед этим переносит в его папку пресеты и подложку
     * из версий до аккаунтов: они достаются аккаунту, который открыли первым.
     */
    suspend fun load(account: String, edited: GamePreset = GamePreset.General): LoadedGame {
        val loaded = repository.load(account, edited)
        repository.adoptLooseFiles(account)
        return loaded
    }

    /** Перечитывает файл аккаунта, ничего не перенося; см. [SlotsRepository.load]. */
    suspend fun refresh(account: String, edited: GamePreset): LoadedGame = repository.load(account, edited)

    /** См. [SlotsRepository.apply]. */
    suspend fun apply(loaded: LoadedGame, layout: SlotLayout, target: GamePreset): String =
        repository.apply(loaded, layout, target)

    /** См. [SlotsRepository.undoLastApply]. */
    suspend fun undoLastApply(account: String): Boolean = repository.undoLastApply(account)

    /** См. [SlotsRepository.readBackground]. */
    suspend fun readBackground(account: String): ByteArray? = repository.readBackground(account)

    /** См. [SlotsRepository.writeBackground]. */
    suspend fun writeBackground(account: String, bytes: ByteArray?) = repository.writeBackground(account, bytes)

    /** См. [DeviceFiles.pickImage]. */
    suspend fun pickImage(): ByteArray? = device.pickImage()
}
