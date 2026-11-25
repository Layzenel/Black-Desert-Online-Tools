package com.layzen.bdotools.core.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class AppThemesTest {
    private class MemorySettings(var id: String? = null) : ThemeSettings {
        override fun saved(): String? = id

        override fun save(id: String) {
            this.id = id
        }
    }

    @Test
    fun startsWithSavedTheme() {
        assertEquals(WindowTheme.Winter, AppThemes(MemorySettings("winter")).selected.value)
    }

    @Test
    fun unknownSavedThemeFallsBackToDefault() {
        assertEquals(WindowTheme.Default, AppThemes(MemorySettings("halloween")).selected.value)
    }

    @Test
    fun selectRemembersTheme() {
        val settings = MemorySettings()
        AppThemes(settings).select(WindowTheme.MorningLand)

        assertEquals("morning", settings.id)
    }
}
