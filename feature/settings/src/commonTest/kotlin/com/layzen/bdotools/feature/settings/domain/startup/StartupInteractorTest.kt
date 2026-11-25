package com.layzen.bdotools.feature.settings.domain.startup

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StartupInteractorTest {
    private class MemoryRepository(var stored: StartupOptions) : StartupRepository {
        var applied = 0

        override suspend fun options(): StartupOptions = stored

        override suspend fun apply(options: StartupOptions) {
            applied++
            stored = options
        }
    }

    @Test
    fun changesOnlyTheRequestedOption() = runTest {
        val repository = MemoryRepository(StartupOptions(supported = true, launchAtStartup = true))
        val result = StartupInteractor(repository).setAlwaysAdmin(true)

        assertEquals(StartupOptions(supported = true, launchAtStartup = true, alwaysAdmin = true), result)
    }

    @Test
    fun startInTrayKeepsOtherOptions() = runTest {
        val repository = MemoryRepository(StartupOptions(supported = true, alwaysAdmin = true))
        val result = StartupInteractor(repository).setStartInTray(true)

        assertEquals(StartupOptions(supported = true, alwaysAdmin = true, startInTray = true), result)
    }

    @Test
    fun unsupportedSystemIsNotTouched() = runTest {
        val repository = MemoryRepository(StartupOptions())
        val result = StartupInteractor(repository).setLaunchAtStartup(true)

        assertEquals(0, repository.applied)
        assertFalse(result.launchAtStartup)
    }
}
