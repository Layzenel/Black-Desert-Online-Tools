package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.feature.uieditor.data.MemorySlotsStorage.Companion.ACCOUNT
import com.layzen.bdotools.feature.uieditor.domain.game.GAME_VARIABLE_SAMPLE
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/** «Откатить применение» возвращает и файл аккаунта, и файлы персонажей, которые переписало применение. */
class UndoApplyTest {

    private val gameVariable = GAME_VARIABLE_SAMPLE +
        "\r\n<UIValue Version=\"4\">\r\n\t<Bool Index=\"1\" Value=\"true\" StorageType=\"1\"/>\r\n\t<Bool Index=\"72\" Value=\"true\" StorageType=\"1\"/>\r\n</UIValue>"
    private val character = "<UIData Index=\"138\" IsShow=\"true\"/>\r\n<UIData Index=\"2\" IsShow=\"false\"/>\r\n"
    private val storage = MemorySlotsStorage(gameVariable, mutableMapOf("11/4000/gamevariable.xml" to character))
    private val repository = SlotsRepositoryImpl(storage)

    @Test
    fun restoresAllFilesWrittenByLastApply() = runTest {
        assertFalse(repository.undoLastApply(ACCOUNT))
        val loaded = repository.load(ACCOUNT)
        repository.apply(loaded, loaded.game.layout.copy(splitHp = true), GamePreset.General)
        assertNotEquals(gameVariable, storage.gameVariable)
        assertNotEquals(character, storage.characters.values.single())

        assertTrue(repository.undoLastApply(ACCOUNT))
        assertEquals(gameVariable, storage.gameVariable)
        assertEquals(character, storage.characters.values.single())
        assertFalse(repository.undoLastApply(ACCOUNT))
    }
}
