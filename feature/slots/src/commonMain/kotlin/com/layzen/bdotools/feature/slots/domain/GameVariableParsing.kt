package com.layzen.bdotools.feature.slots.domain

private val UI_DATA_TAG = Regex("""<UIData\s[^>]*>""")
private val ATTRIBUTE = Regex("""(\w+)="([^"]*)"""")
private val RESOLUTION = Regex("""<Resolution\s+Width="(\d+)"\s+Height="(\d+)"""")
private val UI_SCALE = Regex("""<UiScale\s+Value="([\d.]+)"""")

private val SLOT_INDICES = SlotGroup.entries.flatMap { it.uiIndices }.toSet()

/**
 * Читает экран и записи слотов из текста `gamevariable.xml`. Масштаб и разрешение берутся из
 * первого вхождения: это текущие настройки игры. Без них или без записей слотов —
 * [IllegalArgumentException]: значит, это не тот файл или игра ещё ни разу его не сохраняла.
 */
fun parseGameVariable(text: String): GameVariable {
    val resolution = requireNotNull(RESOLUTION.find(text)) { "Resolution not found" }.groupValues
    val uiScale = UI_SCALE.find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
    val screen = ScreenSpace(resolution[1].toInt(), resolution[2].toInt(), uiScale)
    val uiData = UI_DATA_TAG.findAll(text)
        .mapNotNull { parseUiData(it.value) }
        .filter { it.index in SLOT_INDICES }
        .associateBy { it.index }
    require(uiData.isNotEmpty()) { "No slot UIData found" }
    return GameVariable(screen, uiData)
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
