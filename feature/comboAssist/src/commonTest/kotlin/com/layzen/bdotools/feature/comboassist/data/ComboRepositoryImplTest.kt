package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ComboRepositoryImplTest {

    private class MemoryStorage(var text: String? = null) : ComboStorage {
        override suspend fun read(): String? = text
        override suspend fun write(text: String): Boolean { this.text = text; return true }
    }

    @Test
    fun savesReadsBackAndDeletes() = runTest {
        val storage = MemoryStorage()
        val repository = ComboRepositoryImpl(storage)
        repository.save(StrikerFixture.demo)
        assertEquals(listOf(StrikerFixture.demo), repository.combos())
        repository.save(StrikerFixture.demo.copy(name = "Новое"))
        assertEquals(listOf("Новое"), repository.combos().map { it.name })
        repository.delete("demo")
        assertEquals(emptyList(), repository.combos())
    }

    @Test
    fun brokenFileReadsAsEmpty() = runTest {
        assertEquals(emptyList(), ComboRepositoryImpl(MemoryStorage("{не json")).combos())
    }
}
