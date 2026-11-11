package com.layzen.bdotools.core.account

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CharacterMemosTest {

    private val lobby = "<EnchantPot Version=\"1\"/>\r\n" +
        "<CharacterMemo Version=\"1\">\r\n" +
        "\t<CharacterMemo CharacterNo=\"1\" Memo=\"Коневод\"/>\r\n" +
        "\t<CharacterMemo CharacterNo=\"2\" Memo=\"A &amp; B\"/>\r\n" +
        "</CharacterMemo>\r\n"

    @Test
    fun parsesMemosInFileOrder() {
        assertEquals(mapOf("1" to "Коневод", "2" to "A & B"), parseCharacterMemos(lobby.encodeToByteArray()))
    }

    @Test
    fun replacesOnlyMemoBlockAndKeepsOtherBytes() {
        // байт 0xC0 — кусок cp1251 в соседнем блоке: его нельзя перекодировать
        val prefix = byteArrayOf(0xC0.toByte(), '\r'.code.toByte(), '\n'.code.toByte())
        val file = prefix + lobby.encodeToByteArray()

        val updated = withCharacterMemos(file, mapOf("1" to "Свин \"Золотой\"", "3" to "Рыбак"))

        assertContentEquals(prefix, updated.copyOfRange(0, prefix.size))
        assertEquals(mapOf("1" to "Свин \"Золотой\"", "3" to "Рыбак"), parseCharacterMemos(updated))
        assertEquals(
            "<EnchantPot Version=\"1\"/>\r\n<CharacterMemo Version=\"1\">\r\n" +
                "\t<CharacterMemo CharacterNo=\"1\" Memo=\"Свин &quot;Золотой&quot;\"/>\r\n" +
                "\t<CharacterMemo CharacterNo=\"3\" Memo=\"Рыбак\"/>\r\n</CharacterMemo>\r\n",
            updated.copyOfRange(prefix.size, updated.size).decodeToString(),
        )
    }

    @Test
    fun fillsEmptyBlockAndAppendsMissingOne() {
        val empty = "<A/>\r\n<CharacterMemo Version=\"2\"/>\r\n<B/>\r\n".encodeToByteArray()
        val filled = withCharacterMemos(empty, mapOf("5" to "Чтец")).decodeToString()
        assertEquals("<A/>\r\n<CharacterMemo Version=\"2\">\r\n\t<CharacterMemo CharacterNo=\"5\" Memo=\"Чтец\"/>\r\n</CharacterMemo>\r\n<B/>\r\n", filled)

        val missing = withCharacterMemos("<A/>\r\n".encodeToByteArray(), mapOf("5" to "Чтец"))
        assertEquals(mapOf("5" to "Чтец"), parseCharacterMemos(missing))
    }
}
