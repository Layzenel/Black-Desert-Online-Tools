package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsException
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.CharacterMemosRepository
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemoChange
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.parseCharacterMemos
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.withCharacterMemos

/**
 * Заметки персонажей из окна «Просмотр персонажей аккаунта» игры (`UserCache\<аккаунт>\gameVariable_lobby.xml`).
 * Пока игра запущена, она держит заметки у себя и перезаписывает файл при смене персонажа, телепорте и выходе, поэтому писать стоит при закрытой игре.
 */
class CharacterMemosRepositoryImpl(private val storage: PortraitsStorage) : CharacterMemosRepository {

    override suspend fun memos(account: String): Map<String, String> = parseCharacterMemos(readLobby(account))

    override suspend fun isGameRunning(): Boolean? = storage.isGameRunning()

    override suspend fun write(account: String, changes: List<MemoChange>): String {
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
