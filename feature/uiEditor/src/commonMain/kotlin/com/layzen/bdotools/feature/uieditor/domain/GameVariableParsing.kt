package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.roundToInt

private val ATTRIBUTE = Regex("""(\w+)="([^"]*)"""")
private val RESOLUTION = Regex("""<Resolution\s+Width="(\d+)"\s+Height="(\d+)"""")
private val UI_SCALE = Regex("""<UiScale\s+Value="([\d.]+)"""")

/** «Настройка быстрых слотов» — `<Bool Index="1">` в `<UIValue>` файла аккаунта; найдено сравнением файла до и после переключения. */
internal val CUSTOM_QUICK_SLOTS = Regex("""<Bool\s+Index="1"\s+Value="(true|false)"""")

private val SLOT_INDICES = SlotGroup.entries.flatMap { it.uiIndices }.toSet()

private const val CHAT_INDEX = "32"

/**
 * Читает экран и записи слотов набора [preset] из текста `gamevariable.xml`. Масштаб и разрешение берутся из
 * первого вхождения: это текущие настройки игры. Без них или без записей слотов —
 * [IllegalArgumentException]: значит, это не тот файл или игра ещё ни разу не сохраняла этот набор.
 */
fun parseGameVariable(text: String, preset: GamePreset = GamePreset.General): GameVariable {
    val resolution = requireNotNull(RESOLUTION.find(text)) { "Resolution not found" }.groupValues
    val uiScale = UI_SCALE.find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
    val screen = ScreenSpace(resolution[1].toInt(), resolution[2].toInt(), uiScale)
    val entries = Regex("""<${preset.tag}\s[^>]*>""").findAll(text).map { attributesOf(it.value) }.toList()
    val uiData = entries
        .mapNotNull { parseUiData(it) }
        .filter { it.index in SLOT_INDICES }
        .associateBy { it.index }
    require(uiData.isNotEmpty()) { "No slot ${preset.tag} found" }
    val customQuickSlots = CUSTOM_QUICK_SLOTS.find(text)?.groupValues?.get(1)?.toBoolean()
    return GameVariable(screen, uiData, customQuickSlots, parseChats(entries, screen))
}

/** Все наборы позиций, которые есть в файле: общий обязателен, пресеты 1–3 — если игра их сохраняла. */
fun parseGamePresets(text: String): Map<GamePreset, GameVariable> {
    val general = parseGameVariable(text)
    val presets = GamePreset.entries.drop(1).mapNotNull { preset ->
        runCatching { parseGameVariable(text, preset) }.getOrNull()?.let { preset to it }
    }
    return mapOf(GamePreset.General to general) + presets
}

internal fun attributesOf(tag: String): Map<String, String> =
    ATTRIBUTE.findAll(tag).associate { it.groupValues[1] to it.groupValues[2] }

/**
 * Все чаты (`Index="32"`) по `WindowIndex`: главное окно ([ChatWindow.MAIN]), отдельные окна и вкладки главного
 * (`IsCombinedToMainPanel="true"`). `RelativePosX/Y` — центр окна, как у остальных элементов. Видимость — `IsShow`.
 * Блокировку окна игра хранит не в этом файле, поэтому редактор её не читает и не пишет.
 */
private fun parseChats(entries: List<Map<String, String>>, screen: ScreenSpace): Map<Int, ChatWindow> = entries
    .filter { it["Index"] == CHAT_INDEX }
    .mapNotNull { attributes ->
        val window = attributes["WindowIndex"]?.toIntOrNull() ?: return@mapNotNull null
        val centerX = attributes["RelativePosX"]?.toDoubleOrNull() ?: return@mapNotNull null
        val centerY = attributes["RelativePosY"]?.toDoubleOrNull() ?: return@mapNotNull null
        val chat = ChatWindow(
            center = SlotPosition(((centerX - 0.5) * screen.width).roundToInt(), ((centerY - 0.5) * screen.height).roundToInt()),
            width = attributes["SizeX"]?.toIntOrNull() ?: return@mapNotNull null,
            height = attributes["SizeY"]?.toIntOrNull() ?: return@mapNotNull null,
            visible = attributes["IsShow"] == "true",
            combined = window != ChatWindow.MAIN && attributes["IsCombinedToMainPanel"] == "true",
        )
        window to chat
    }
    .sortedBy { it.first }
    .toMap()

private fun parseUiData(attributes: Map<String, String>): UiData? {
    return UiData(
        index = attributes["Index"]?.toIntOrNull() ?: return null,
        isShow = attributes["IsShow"] == "true",
        pendingType = attributes["PendingType"] ?: return null,
        posX = attributes["PosX"]?.toIntOrNull() ?: return null,
        posY = attributes["PosY"]?.toIntOrNull() ?: return null,
        relativeX = attributes["RelativePosX"]?.toDoubleOrNull() ?: return null,
        relativeY = attributes["RelativePosY"]?.toDoubleOrNull() ?: return null,
    )
}
