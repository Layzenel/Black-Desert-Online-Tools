package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.feature.portraitconstructor.domain.MemoChange
import com.layzen.bdotools.feature.portraitconstructor.domain.parseCharacterMemos
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class CharacterMemosRepositoryTest {

    private val original = ("<CharacterMemo Version=\"1\">\r\n" +
        "\t<CharacterMemo CharacterNo=\"1\" Memo=\"Коневод\"/>\r\n" +
        "\t<CharacterMemo CharacterNo=\"2\" Memo=\"Золотой свин\"/>\r\n" +
        "</CharacterMemo>\r\n").encodeToByteArray()

    @Test
    fun writesChangesAndKeepsOtherMemosAndBackup() = runTest {
        val storage = MemoryStorage().apply { lobby["acc"] = original }
        val repository = CharacterMemosRepository(storage)

        val backup = repository.write("acc", listOf(MemoChange("2", "Золотой свин", "Свин"), MemoChange("3", "", "Рыбак")))

        assertContentEquals(original, storage.lobbyBackups.getValue(backup))
        assertEquals(mapOf("1" to "Коневод", "2" to "Свин", "3" to "Рыбак"), parseCharacterMemos(storage.lobby.getValue("acc")))
        assertEquals(mapOf("1" to "Коневод", "2" to "Свин", "3" to "Рыбак"), repository.memos("acc"))
    }

    @Test
    fun refusesToWriteWhileGameIsRunning() = runTest {
        val storage = MemoryStorage().apply {
            lobby["acc"] = original
            gameRunning = true
        }

        val error = assertFailsWith<PortraitsException> {
            CharacterMemosRepository(storage).write("acc", listOf(MemoChange("3", "", "Рыбак")))
        }

        assertEquals(PortraitsError.GameRunning, error.error)
        assertContentEquals(original, storage.lobby.getValue("acc"))
        assertEquals(emptyMap(), storage.lobbyBackups)
    }

    @Test
    fun missingLobbyFileIsReported() = runTest {
        val error = assertFailsWith<PortraitsException> { CharacterMemosRepository(MemoryStorage()).memos("acc") }
        assertEquals(PortraitsError.LobbyNotFound, error.error)
    }
}
