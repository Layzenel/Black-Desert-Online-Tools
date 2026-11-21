package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PingSettingsRepositoryImplTest {
    /** Хранилище в памяти. */
    private class MemoryStorage(var text: String? = null) : PingSettingsStorage {
        override fun read(): String? = text
        override fun write(text: String) { this.text = text }
    }

    @Test
    fun savedSettingsReadBack() = runTest {
        val repository = PingSettingsRepositoryImpl(MemoryStorage())
        repository.save(PingSettings(shown = true, scale = 150, opacity = 70, fpsShown = true))
        assertEquals(PingSettings(true, 150, 70, fpsShown = true), repository.settings())
    }

    @Test
    fun settingsSavedBeforeFpsReadWithFpsOff() = runTest {
        assertEquals(PingSettings(true, 80, 60, fpsShown = false), PingSettingsRepositoryImpl(MemoryStorage("true,80,60")).settings())
    }

    @Test
    fun brokenTextIsDefault() = runTest {
        assertEquals(PingSettings(), PingSettingsRepositoryImpl(MemoryStorage("yes,big")).settings())
    }
}
