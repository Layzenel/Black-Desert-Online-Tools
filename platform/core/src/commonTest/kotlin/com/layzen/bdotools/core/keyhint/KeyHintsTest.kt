package com.layzen.bdotools.core.keyhint

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyHintsTest {
    private class MemorySettings(var encoded: String? = null) : KeyHintSettings {
        override fun saved(): String? = encoded

        override fun save(encoded: String) {
            this.encoded = encoded
        }
    }

    @Test
    fun encodeAndDecodeRoundTrip() {
        val hints = KeyHints(KeyHintStyle.Short, KeyHintStyle.Icon, KeyHintStyle.Text)

        assertEquals(hints, KeyHints.decode(hints.encode()))
    }

    @Test
    fun brokenOrMissingSettingFallsBackToText() {
        assertEquals(KeyHints.Default, KeyHints.decode(null))
        assertEquals(KeyHints(shift = KeyHintStyle.Icon), KeyHints.decode("icon,oops"))
    }

    @Test
    fun styleOfPicksKeyAndKeepsShortForMouse() {
        val hints = KeyHints(KeyHintStyle.Short, KeyHintStyle.Short, KeyHintStyle.Icon)

        assertEquals(KeyHintStyle.Short, hints.styleOf(KEY_CODE_SHIFT))
        assertEquals(KeyHintStyle.Short, hints.styleOf(KEY_CODE_LMB))
        assertEquals(KeyHintStyle.Icon, hints.styleOf(KEY_CODE_RMB))
        assertEquals(KeyHintStyle.Text, hints.styleOf(81))
    }

    @Test
    fun updateRemembersChoice() {
        val settings = MemorySettings()
        val hints = AppKeyHints(settings)
        hints.update { it.copy(rmb = KeyHintStyle.Icon) }

        assertEquals("text,text,icon", settings.encoded)
        assertEquals(KeyHintStyle.Icon, hints.selected.value.rmb)
    }
}
