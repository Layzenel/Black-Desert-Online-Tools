package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.feature.portraitconstructor.domain.MemoChange
import com.layzen.bdotools.feature.portraitconstructor.domain.parseCharacterMemos
import com.layzen.bdotools.feature.portraitconstructor.domain.withCharacterMemos

/**
 * Заметки персонажей из окна «Просмотр персонажей аккаунта» игры (`UserCache\<аккаунт>\gameVariable_lobby.xml`).
 * Пока игра запущена, она держит заметки у себя и перезаписывает файл при смене персонажа, телепорте и выходе, поэтому писать стоит при закрытой игре.
 */
class CharacterMemosRepository(private val storage: PortraitsStorage) {

    /** Текущие заметки аккаунта: ID персонажа → заметка. */
    suspend fun memos(account: String): Map<String, String> = parseCharacterMemos(readLobby(account))

    /** См. [PortraitsStorage.isGameRunning]. */
    suspend fun isGameRunning(): Boolean? = storage.isGameRunning()

    /**
     * Сохраняет копию файла, затем записывает в него [changes]. Файл перечитывается, чтобы не затереть свежие заметки.
     * Запущенная игра — [PortraitsError.GameRunning]: она всё равно перезаписала бы файл при смене персонажа, телепорте или выходе.
     */
    suspend fun write(account: String, changes: List<MemoChange>): String {
        if (storage.isGameRunning() == true) throw PortraitsException(PortraitsError.GameRunning)
        val file = readLobby(account)
        val backup = storage.backupLobbyVariables(account, file)
        val memos = parseCharacterMemos(file) + changes.associate { it.characterId to it.new }
        storage.writeLobbyVariables(account, withCharacterMemos(file, memos))
        return backup
    }

    private suspend fun readLobby(account: String): ByteArray =
        storage.readLobbyVariables(account) ?: throw PortraitsException(PortraitsError.LobbyNotFound)
}
