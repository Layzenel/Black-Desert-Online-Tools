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
        val hints = KeyHints(KeyHintStyle.Short, KeyHintStyle.Icon, KeyHintStyle.Text, KeyHintStyle.Icon)

        assertEquals(hints, KeyHints.decode(hints.encode()))
    }

    @Test
    fun oldSettingWithoutMovementKeysShowsLetters() {
        assertEquals(KeyHints(rmb = KeyHintStyle.Icon), KeyHints.decode("text,text,icon"))
    }

    @Test
    fun movementKeysShareOneStyle() {
        val hints = KeyHints(move = KeyHintStyle.Icon)

        listOf(KEY_CODE_W, KEY_CODE_A, KEY_CODE_S, KEY_CODE_D).forEach { assertEquals(KeyHintStyle.Icon, hints.styleOf(it)) }
        assertEquals(KeyHintStyle.Text, hints.styleOf(KEY_CODE_SHIFT))
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

        assertEquals("text,text,icon,text", settings.encoded)
        assertEquals(KeyHintStyle.Icon, hints.selected.value.rmb)
    }
}
