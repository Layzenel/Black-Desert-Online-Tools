package com.layzen.bdotools.feature.slots.domain

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Перевод раскладки в JSON-файл [LayoutFile] и обратно. */
object LayoutCodec {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(layout: NamedLayout): String = json.encodeToString(
        LayoutFile.serializer(),
        LayoutFile(
            name = layout.name,
            cooldown = layout.layout.slotsOf(SlotGroup.Cooldown),
            hotbar = layout.layout.slotsOf(SlotGroup.Hotbar),
        ),
    )

    /** Читает файл раскладки. Слоты с номерами вне 1–20 пропускаются. Ошибка — [LayoutFormatException]. */
    fun decode(text: String): NamedLayout {
        val file = parse(text)
        val positions = file.cooldown.toPositions(SlotGroup.Cooldown) + file.hotbar.toPositions(SlotGroup.Hotbar)
        return NamedLayout(file.name, SlotLayout(positions))
    }

    private fun parse(text: String): LayoutFile {
        // Сначала только заголовок: чужой JSON должен давать понятную ошибку, а не «не тот тип поля»
        val (format, version) = try {
            val header = json.parseToJsonElement(text).jsonObject
            header["format"]?.jsonPrimitive?.contentOrNull to header["version"]?.jsonPrimitive?.intOrNull
        } catch (e: IllegalArgumentException) {
            throw LayoutFormatException(LayoutError.NotJson, e.message)
        }
        if (format != LayoutFile.FORMAT) throw LayoutFormatException(LayoutError.WrongFormat)
        if (version != LayoutFile.VERSION) throw LayoutFormatException(LayoutError.UnsupportedVersion)
        return try {
            json.decodeFromString(LayoutFile.serializer(), text)
        } catch (e: SerializationException) {
            throw LayoutFormatException(LayoutError.NotJson, e.message)
        }
    }

    private fun SlotLayout.slotsOf(group: SlotGroup): List<LayoutSlot> =
        group.slots.mapNotNull { id -> this[id]?.let { LayoutSlot(id.number, it.x, it.y) } }

    private fun List<LayoutSlot>.toPositions(group: SlotGroup): Map<SlotId, SlotPosition> =
        filter { it.slot in 1..group.uiIndices.size }
            .associate { SlotId(group, it.slot) to SlotPosition(it.x, it.y) }
}
