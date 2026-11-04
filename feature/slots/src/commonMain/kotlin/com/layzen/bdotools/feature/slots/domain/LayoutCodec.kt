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
            classResource = layout.layout.slotsOf(SlotGroup.ClassResource),
        ),
    )

    /**
     * Читает файл раскладки. Элементы с номерами вне группы пропускаются. Ошибка — [LayoutFormatException];
     * неизвестный якорь — [LayoutError.WrongFormat].
     */
    fun decode(text: String): NamedLayout {
        val file = parse(text)
        val slots = SlotGroup.entries.flatMap { group ->
            file.slotsOf(group).filter { it.slot in 1..group.uiIndices.size }.map { SlotId(group, it.slot) to it }
        }
        val layout = AnchoredLayout(
            positions = slots.associate { (id, slot) -> id to AnchoredPosition(slot.anchorOrFail(), slot.x, slot.y) },
            hidden = slots.filter { (_, slot) -> !slot.visible }.mapTo(mutableSetOf()) { it.first },
        )
        return NamedLayout(file.name, layout)
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
        if (version == null || version !in LayoutFile.SUPPORTED_VERSIONS) throw LayoutFormatException(LayoutError.UnsupportedVersion)
        return try {
            json.decodeFromString(LayoutFile.serializer(), text)
        } catch (e: SerializationException) {
            throw LayoutFormatException(LayoutError.NotJson, e.message)
        }
    }

    private fun LayoutSlot.anchorOrFail(): SlotAnchor =
        SlotAnchor.byId(anchor) ?: throw LayoutFormatException(LayoutError.WrongFormat, "Unknown anchor $anchor")

    private fun AnchoredLayout.slotsOf(group: SlotGroup): List<LayoutSlot> = group.slots.mapNotNull { id ->
        positions[id]?.let { LayoutSlot(id.number, it.anchor.id, it.x, it.y, id !in hidden) }
    }
}
