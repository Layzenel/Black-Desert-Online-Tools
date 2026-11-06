package com.layzen.bdotools.feature.uieditor.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatWindowTest {

    /** Экран 2560×1440 при масштабе 0.8 — 3200×1800 UI-px. */
    private val screen = ScreenSpace(2560, 1440, 0.8)

    /** Записи чатов из настоящего файла: вкладка главного окна, отдельное окно у правого края и главное окно. */
    private val sample = GAME_VARIABLE_SAMPLE + listOf(
        """	<UIData Index="32" IsShow="false" WindowIndex="1" IsUsing="true" IsCombinedToMainPanel="true" PendingType="LeftBottom" PosX="42" PosY="-60" RelativePosX="0.0787499994" RelativePosY="0.894503057" SizeX="420" SizeY="222" FontSize="6750318" Transparency="0.5" LangType="1">""",
        """	<UIData Index="32" IsShow="false" WindowIndex="4" IsUsing="true" IsCombinedToMainPanel="false" PendingType="RightBottom" PosX="37" PosY="32" RelativePosX="0.8939062357" RelativePosY="0.8789561391" SizeX="675" SizeY="350" FontSize="0" Transparency="0.5" LangType="1">""",
        """	<UIData Index="32" IsShow="true" WindowIndex="0" IsUsing="true" IsCombinedToMainPanel="true" PendingType="LeftBottom" PosX="0" PosY="23" RelativePosX="0.1034374982" RelativePosY="0.8842309713" SizeX="662" SizeY="351" FontSize="6750318" Transparency="0.5" LangType="1">""",
        """	<UISettingPreset0 Index="128" IsShow="true" IsOpen="true" PendingType="LeftTop" PosX="1086" PosY="1039" RelativePosX="0.339375" RelativePosY="0.577457"/>""",
        """	<UISettingPreset0 Index="32" IsShow="true" WindowIndex="0" IsOpen="false" IsCombinedToMainPanel="true" PendingType="LeftTop" PosX="0" PosY="1417" RelativePosX="0.103437" RelativePosY="0.884231" SizeX="662" SizeY="351">""",
    ).joinToString("\r\n", postfix = "\r\n")

    @Test
    fun readsAllChatsWithTabsMarked() {
        val chats = parseGameVariable(sample).chats
        assertEquals(listOf(0, 1, 4), chats.keys.toList())
        assertEquals(listOf(false, true, false), chats.values.map { it.combined })
        val main = chats.getValue(0)
        // Главное окно прижато к левому краю экрана
        assertEquals(0.0, main.left(screen), 1.0)
        assertEquals(662 to 351, main.width to main.height)
        assertTrue(main.visible)
        assertEquals(3200.0, chats.getValue(4).left(screen) + 675, 3.0)
        assertFalse(chats.getValue(4).visible)
    }

    @Test
    fun hiddenMainChatHidesSideWindows() {
        val side = ChatWindow(SlotPosition(1000, 500), 400, 300)
        val layout = SlotLayout(chats = mapOf(ChatWindow.MAIN to side, 4 to side, 5 to side.copy(visible = false)))
        assertTrue(layout.isChatShown(4))
        assertFalse(layout.isChatShown(5))
        val mainHidden = layout.withChat(ChatWindow.MAIN, side.copy(visible = false))
        assertFalse(mainHidden.isChatShown(4))
        // Своя видимость побочного окна при этом не меняется
        assertTrue(mainHidden.chats.getValue(4).visible)
    }

    @Test
    fun detachedTabStandsAboveMainWindow() {
        val main = ChatWindow(SlotPosition(-1269, 691), 662, 351)
        val tab = ChatWindow(SlotPosition(0, 0), 420, 222, visible = false, combined = true)
        val layout = SlotLayout(chats = mapOf(ChatWindow.MAIN to main, 1 to tab))
        assertFalse(layout.isChatShown(1))
        val detached = layout.withDetachedChat(1)
        val window = detached.chats.getValue(1)
        assertFalse(window.combined)
        assertTrue(detached.isChatShown(1))
        assertEquals(main.left(screen), window.left(screen), 1.0)
        assertEquals(main.top(screen), window.top(screen) + window.height, 1.0)
        assertEquals(420 to 222, window.width to window.height)
    }

    @Test
    fun separateWindowGoesBackIntoMain() {
        val main = ChatWindow(SlotPosition(-1269, 691), 662, 351)
        val side = ChatWindow(SlotPosition(1000, 600), 500, 250)
        val layout = SlotLayout(chats = mapOf(ChatWindow.MAIN to main, 4 to side))
        val combined = layout.withCombinedChat(4)
        assertTrue(combined.chats.getValue(4).combined)
        assertFalse(combined.isChatShown(4))
        // Главное окно не вкладывается, а при новом выносе окно возвращается своего размера
        assertEquals(layout, layout.withCombinedChat(ChatWindow.MAIN))
        assertEquals(500 to 250, combined.withDetachedChat(4).chats.getValue(4).let { it.width to it.height })
    }

    @Test
    fun resizeFromTopRightKeepsBottomLeft() {
        val chat = ChatWindow(SlotPosition(0, 0), 400, 300)
        val resized = chat.resizedFromTopRight(100, -60, screen)
        assertEquals(500 to 360, resized.width to resized.height)
        assertEquals(chat.left(screen), resized.left(screen))
        assertEquals(chat.top(screen) + chat.height, resized.top(screen) + resized.height)
    }

    @Test
    fun resizeStopsAtLimits() {
        val chat = ChatWindow(SlotPosition(0, 0), 400, 300)
        assertEquals(CHAT_MIN_WIDTH to CHAT_MIN_HEIGHT, chat.resizedFromTopRight(-1000, 1000, screen).let { it.width to it.height })
        assertEquals(ChatWindow.maxWidth(screen), chat.resizedFromTopRight(5000, 0, screen).width)
    }

    @Test
    fun typedSizeKeepsBottomLeftAndLimits() {
        val chat = ChatWindow(SlotPosition(0, 0), 400, 300)
        val sized = chat.withSize(500, 50, screen)
        assertEquals(500 to CHAT_MIN_HEIGHT, sized.width to sized.height)
        assertEquals(chat.bounds.left, sized.bounds.left)
        assertEquals(chat.bounds.bottom, sized.bounds.bottom)
    }

    @Test
    fun snapLinesIncludeShownChatsButNotMovingOne() {
        val main = ChatWindow(SlotPosition(-1000, 600), 600, 300)
        val tab = ChatWindow(SlotPosition(0, 0), 420, 222, combined = true)
        val side = ChatWindow(SlotPosition(1000, 600), 400, 200)
        val layout = SlotLayout(chats = mapOf(0 to main, 1 to tab, 4 to side))
        val lines = layout.snapLines(emptyList(), emptySet(), movingChats = setOf(4), crosshair = SlotPosition(0, -131))
        assertEquals(listOf(0.0, -1300.0, -1000.0, -700.0), lines.x)
        assertEquals(listOf(-131.0, 450.0, 600.0, 750.0), lines.y)
    }

    @Test
    fun importTakesOnlyOwnChats() {
        val own = ChatWindow(SlotPosition(-1000, 600), 600, 300)
        val foreign = ChatWindow(SlotPosition(500, 500), 700, 400, visible = false)
        val mine = SlotLayout(chats = mapOf(0 to own))
        val imported = SlotLayout(chats = mapOf(0 to foreign, 4 to foreign))
        assertEquals(mapOf(0 to foreign), mine.overlaidWith(imported).chats)
    }

    @Test
    fun chatsSurviveExportAndImport() {
        val chat = ChatWindow(SlotPosition(-1269, 691), 662, 351, visible = false)
        val tab = ChatWindow(SlotPosition(0, 0), 420, 222, combined = true)
        val chats = mapOf(0 to chat, 2 to tab)
        val anchored = AnchoredLayout.of(SlotLayout(chats = chats), screen)
        val decoded = LayoutCodec.decode(LayoutCodec.encode(NamedLayout("чаты", anchored)))
        assertEquals(chats, decoded.layout.placeOn(screen).chats)
    }
}
