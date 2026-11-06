package com.layzen.bdotools.feature.uieditor.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatWritingTest {

    private val screen = ScreenSpace(2560, 1440, 0.8)

    private val tab = """	<UIData Index="32" IsShow="false" WindowIndex="1" IsUsing="true" IsCombinedToMainPanel="true" PendingType="LeftBottom" PosX="42" PosY="-60" RelativePosX="0.0787499994" RelativePosY="0.894503057" SizeX="420" SizeY="222" FontSize="6750318" Transparency="0.5" LangType="1">"""
    private val main = """	<UIData Index="32" IsShow="true" WindowIndex="0" IsUsing="true" IsCombinedToMainPanel="true" PendingType="LeftBottom" PosX="0" PosY="23" RelativePosX="0.1034374982" RelativePosY="0.8842309713" SizeX="662" SizeY="351" FontSize="6750318" Transparency="0.5" LangType="1">"""
    private val text = GAME_VARIABLE_SAMPLE + listOf(tab, """		<ChatType key="Notice" value="true" colorIndexValue="-1"/>""", main).joinToString("\r\n", postfix = "\r\n")

    @Test
    fun unchangedChatsKeepFileAsIs() {
        val game = parseGameVariable(text)
        assertEquals(text, writeChats(text, game.chats, screen))
    }

    @Test
    fun detachedTabIsWrittenAndReadBack() {
        val layout = parseGameVariable(text).layout.withDetachedChat(1)
        val written = writeChats(text, parseGameVariable(text).changedChats(layout), screen)
        val reread = parseGameVariable(written).chats
        assertEquals(layout.chats.getValue(1), reread.getValue(1))
        // Главное окно и остальной текст не тронуты
        assertTrue(main in written)
        assertEquals(text.lines().size, written.lines().size)
    }

    @Test
    fun resizedMainKeepsOtherAttributes() {
        val chat = parseGameVariable(text).chats.getValue(0).withSize(800, 400, screen)
        val written = writeChats(text, mapOf(0 to chat), screen)
        val line = written.lines().first { "WindowIndex=\"0\"" in it }
        assertTrue("""SizeX="800" SizeY="400" FontSize="6750318"""" in line)
        assertTrue("""IsCombinedToMainPanel="true"""" in line)
        assertEquals(chat, parseGameVariable(written).chats.getValue(0))
    }

    @Test
    fun characterVisibilityFollowsAccountOrder() {
        val character = "<UIData Index=\"32\" IsShow=\"false\"/>\r\n<UIData Index=\"32\" IsShow=\"true\"/>\r\n<Avatar Index=\"32\" IsOn=\"false\"/>\r\n"
        val order = chatWindowOrder(text)
        assertEquals(listOf(1, 0), order)
        val expected = "<UIData Index=\"32\" IsShow=\"true\"/>\r\n<UIData Index=\"32\" IsShow=\"true\"/>\r\n<Avatar Index=\"32\" IsOn=\"false\"/>\r\n"
        assertEquals(expected, writeChatVisibility(character, order, mapOf(1 to true)))
    }
}
