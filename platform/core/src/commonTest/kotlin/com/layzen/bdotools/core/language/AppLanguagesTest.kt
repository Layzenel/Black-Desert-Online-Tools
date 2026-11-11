package com.layzen.bdotools.core.language

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguagesTest {

    private class MemorySettings(var saved: String?, val system: String) : LanguageSettings {
        override fun saved(): String? = saved

        override fun save(tag: String) {
            saved = tag
        }

        override fun system(): String = system
    }

    @Test
    fun savedLanguageWinsOverSystem() {
        assertEquals(AppLanguage.Russian, AppLanguages(MemorySettings("ru", "en-US")).selected.value)
    }

    @Test
    fun systemLanguageIsUsedWithoutSavedChoice() {
        assertEquals(AppLanguage.Russian, AppLanguages(MemorySettings(null, "ru_RU")).selected.value)
    }

    @Test
    fun unknownLanguagesFallBack() {
        assertEquals(AppLanguage.Fallback, AppLanguages(MemorySettings("xx", "de-DE")).selected.value)
    }

    @Test
    fun selectSavesChoice() {
        val settings = MemorySettings(null, "en")
        val languages = AppLanguages(settings)
        languages.select(AppLanguage.Russian)
        assertEquals(AppLanguage.Russian, languages.selected.value)
        assertEquals("ru", settings.saved)
    }
}
