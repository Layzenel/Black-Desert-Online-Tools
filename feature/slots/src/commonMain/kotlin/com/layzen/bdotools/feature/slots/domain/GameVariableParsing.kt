package com.layzen.bdotools.feature.slots.domain

private val ATTRIBUTE = Regex("""(\w+)="([^"]*)"""")
private val RESOLUTION = Regex("""<Resolution\s+Width="(\d+)"\s+Height="(\d+)"""")
private val UI_SCALE = Regex("""<UiScale\s+Value="([\d.]+)"""")

private val SLOT_INDICES = SlotGroup.entries.flatMap { it.uiIndices }.toSet()

/**
 * Читает экран и записи слотов набора [preset] из текста `gamevariable.xml`. Масштаб и разрешение берутся из
 * первого вхождения: это текущие настройки игры. Без них или без записей слотов —
 * [IllegalArgumentException]: значит, это не тот файл или игра ещё ни разу не сохраняла этот набор.
 */
fun parseGameVariable(text: String, preset: GamePreset = GamePreset.General): GameVariable {
    val resolution = requireNotNull(RESOLUTION.find(text)) { "Resolution not found" }.groupValues
    val uiScale = UI_SCALE.find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
    val screen = ScreenSpace(resolution[1].toInt(), resolution[2].toInt(), uiScale)
    val uiData = Regex("""<${preset.tag}\s[^>]*>""").findAll(text)
        .mapNotNull { parseUiData(it.value) }
        .filter { it.index in SLOT_INDICES }
        .associateBy { it.index }
    require(uiData.isNotEmpty()) { "No slot ${preset.tag} found" }
    return GameVariable(screen, uiData)
}

/** Все наборы позиций, которые есть в файле: общий обязателен, пресеты 1–3 — если игра их сохраняла. */
fun parseGamePresets(text: String): Map<GamePreset, GameVariable> {
    val general = parseGameVariable(text)
    val presets = GamePreset.entries.drop(1).mapNotNull { preset ->
        runCatching { parseGameVariable(text, preset) }.getOrNull()?.let { preset to it }
    }
    return mapOf(GamePreset.General to general) + presets
}

private fun parseUiData(tag: String): UiData? {
    val attributes = ATTRIBUTE.findAll(tag).associate { it.groupValues[1] to it.groupValues[2] }
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
