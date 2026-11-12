package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.layout.CHAT_MIN_HEIGHT
import com.layzen.bdotools.feature.uieditor.domain.layout.CHAT_MIN_WIDTH
import com.layzen.bdotools.feature.uieditor.domain.layout.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.screen.SlotAnchor
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.domain.skill.isCp1251
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

    /** Тот же формат одной строкой и без значений по умолчанию: короче, чтобы вставить в сообщение чата. */
    private val compactJson = Json(json) {
        prettyPrint = false
        encodeDefaults = false
    }

    /**
     * Превращает раскладку [layout] в JSON-текст файла `*.json` для экспорта и папки пресетов; [compact] — одной
     * строкой для буфера обмена. [decode] читает оба вида.
     */
    fun encode(layout: NamedLayout, compact: Boolean = false): String = (if (compact) compactJson else json).encodeToString(
        LayoutFile.serializer(),
        LayoutFile(
            name = layout.name,
            cooldown = layout.layout.slotsOf(SlotGroup.Cooldown),
            hotbar = layout.layout.slotsOf(SlotGroup.Hotbar),
            classResource = layout.layout.slotsOf(SlotGroup.ClassResource),
            interfaceElements = layout.layout.slotsOf(SlotGroup.Interface),
            customQuickSlots = layout.layout.customQuickSlots,
            chats = layout.layout.chats.map { (window, chat) ->
                LayoutChat(window, chat.position.anchor.id, chat.position.x, chat.position.y, chat.width, chat.height, chat.visible, chat.combined)
            },
            splitHp = layout.layout.splitHp,
            skills = true.takeIf { layout.layout.skills != null },
            memos = true.takeIf { layout.layout.memos != null },
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
            positions = slots.associate { (id, slot) -> id to AnchoredPosition(anchorOrFail(slot.anchor), slot.x, slot.y) },
            hidden = slots.filter { (_, slot) -> !slot.visible }.mapTo(mutableSetOf()) { it.first },
            labels = slots.mapNotNull { (id, slot) -> slot.labelOf(id)?.let { id to it } }.toMap(),
            customQuickSlots = file.customQuickSlots,
            chats = file.chats.associate { it.window to it.toAnchored() },
            splitHp = file.splitHp,
            skills = if (file.skills == true) cooldownSkills(slots) else null,
            memos = if (file.memos == true) cooldownMemos(slots) else null,
        )
        return NamedLayout(file.name, layout)
    }

    /** Навыки слотов перезарядки из файла: у других групп навыков не бывает. */
    private fun cooldownSkills(slots: List<Pair<SlotId, LayoutSlot>>): Map<SlotId, Int> = slots.mapNotNull { (id, slot) ->
        slot.skill?.takeIf { id.group == SlotGroup.Cooldown && it > 0 }?.let { id to it }
    }.toMap()

    /** Надписи навыков из слотов перезарядки: только знаки, которые игра покажет, и не длиннее, чем она позволяет. */
    private fun cooldownMemos(slots: List<Pair<SlotId, LayoutSlot>>): Map<Int, String> = slots.mapNotNull { (id, slot) ->
        val skill = slot.skill?.takeIf { id.group == SlotGroup.Cooldown } ?: return@mapNotNull null
        val memo = slot.memo?.filter(::isCp1251)?.take(SlotLabel.MAX_CHARS)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        skill to memo
    }.toMap()

    private fun LayoutChat.toAnchored() = AnchoredChat(
        AnchoredPosition(anchorOrFail(anchor), x, y),
        width.coerceAtLeast(CHAT_MIN_WIDTH),
        height.coerceAtLeast(CHAT_MIN_HEIGHT),
        visible,
        combined = combined && window != ChatWindow.MAIN,
    )

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

    private fun anchorOrFail(id: String): SlotAnchor =
        SlotAnchor.byId(id) ?: throw LayoutFormatException(LayoutError.WrongFormat, "Unknown anchor $id")

    /** Ширина надписи из файла, приведённая к допустимой; у элементов, которым надпись не положена, — `null`. */
    private fun LayoutSlot.labelOf(id: SlotId): Int? =
        labelChars?.takeIf { SlotLabel.allowedFor(id) }?.coerceIn(SlotLabel.CHARS_RANGE)

    private fun AnchoredLayout.slotsOf(group: SlotGroup): List<LayoutSlot> = group.slots.mapNotNull { id ->
        val skill = skills?.get(id)
        positions[id]?.let { LayoutSlot(id.number, it.anchor.id, it.x, it.y, id !in hidden, labels[id], skill, skill?.let { memos?.get(it) }) }
    }
}
