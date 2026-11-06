package com.layzen.bdotools.feature.uieditor.domain

private val CHAT_TAG_OF_CHARACTER = Regex("""<UIData\s+Index="32"[^>]*>""")
private val WINDOW_INDEX = Regex("""\bWindowIndex="(\d+)"""")

/**
 * Записывает окна чата [chats] (`WindowIndex` → окно) в набор [preset] файла аккаунта на экране [screen]:
 * центр (`RelativePosX/Y`, `PosX/PosY` сдвигаются на ту же величину, как у слотов, см. [movedTo]), размер
 * (`SizeX/SizeY`), видимость (`IsShow`), вкладка ли это главного окна (`IsCombinedToMainPanel`, у главного не трогаем).
 * Остальной текст — байт в байт прежний.
 */
fun writeChats(text: String, chats: Map<Int, ChatWindow>, screen: ScreenSpace, preset: GamePreset = GamePreset.General): String =
    chats.entries.fold(text) { current, (window, chat) ->
        Regex("""<${preset.tag}\s+Index="32"\s[^>]*?\bWindowIndex="$window"[^>]*>""").replace(current) { match ->
            chatTag(match.value, window, chat, screen, preset)
        }
    }

/**
 * Порядок окон чата в файле аккаунта: `WindowIndex` записей `<UIData Index="32">` сверху вниз. В файлах персонажей
 * у этих записей нет `WindowIndex`, зато они идут в том же порядке.
 * shortcut: порядок совпал во всех файлах персонажей 2026-11-06; если игра начнёт его менять, видимость попадёт не тому окну.
 */
fun chatWindowOrder(accountText: String): List<Int> = Regex("""<UIData\s+Index="32"\s[^>]*>""").findAll(accountText)
    .mapNotNull { WINDOW_INDEX.find(it.value)?.groupValues?.get(1)?.toIntOrNull() }
    .toList()

/**
 * Видимость окон чата [visibility] в файле персонажа: n-я запись `<UIData Index="32">` — окно `order[n]`,
 * см. [chatWindowOrder]. Записей, которых нет, это не добавляет.
 */
fun writeChatVisibility(text: String, order: List<Int>, visibility: Map<Int, Boolean>): String {
    var n = 0
    return CHAT_TAG_OF_CHARACTER.replace(text) { match ->
        val visible = order.getOrNull(n++)?.let { visibility[it] }
        if (visible == null) match.value else match.value.withAttribute("IsShow", visible.toString())
    }
}

private fun chatTag(tag: String, window: Int, chat: ChatWindow, screen: ScreenSpace, preset: GamePreset): String {
    val attributes = attributesOf(tag)
    val written = UiData(
        index = 32,
        isShow = chat.visible,
        pendingType = attributes["PendingType"].orEmpty(),
        posX = attributes["PosX"]?.toIntOrNull() ?: 0,
        posY = attributes["PosY"]?.toIntOrNull() ?: 0,
        relativeX = attributes["RelativePosX"]?.toDoubleOrNull() ?: 0.0,
        relativeY = attributes["RelativePosY"]?.toDoubleOrNull() ?: 0.0,
    )
    var changed = tag.withAttribute("IsShow", chat.visible.toString())
    // Координаты — только если окно сдвинули: иначе пересчёт округлением сдвинул бы их на долю пикселя
    if (written.position(screen) != chat.center) {
        val moved = written.movedTo(chat.center, screen)
        changed = changed.withAttribute("PosX", moved.posX.toString())
            .withAttribute("PosY", moved.posY.toString())
            .withAttribute("RelativePosX", moved.relativeX.toFixed(preset.relativeDigits))
            .withAttribute("RelativePosY", moved.relativeY.toFixed(preset.relativeDigits))
    }
    changed = changed.withAttribute("SizeX", chat.width.toString())
        .withAttribute("SizeY", chat.height.toString())
    if (window != ChatWindow.MAIN) changed = changed.withAttribute("IsCombinedToMainPanel", chat.combined.toString())
    return changed
}
