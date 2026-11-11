package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Файл раскладки `*.json` — то, что экспортируется, импортируется и лежит в папке пресетов.
 *
 * Каждый элемент хранит якорь (`crosshair`, `top-left`, `top-right`, `bottom-left`, `bottom-right`) и смещение
 * центра от него в UI-px, поэтому раскладка переносится на монитор с другим разрешением и масштабом интерфейса.
 * Группы, которых нет в файле, при импорте не меняются. [customQuickSlots] — «Настройка быстрых слотов»
 * ([SlotLayout.customQuickSlots]); нет в файле — при импорте не меняется.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LayoutFile(
    val format: String = FORMAT,
    val version: Int = VERSION,
    val name: String,
    val units: String = "ui-px",
    val cooldown: List<LayoutSlot> = emptyList(),
    val hotbar: List<LayoutSlot> = emptyList(),
    val classResource: List<LayoutSlot> = emptyList(),
    /** Прочие элементы; `slot` — номер элемента в [InterfaceElement] с единицы. */
    val interfaceElements: List<LayoutSlot> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) val customQuickSlots: Boolean? = null,
    /** Окна чата; при импорте меняются только те, что есть у игрока. */
    val chats: List<LayoutChat> = emptyList(),
) {
    /** Элементы группы [group] из файла. */
    fun slotsOf(group: SlotGroup): List<LayoutSlot> = when (group) {
        SlotGroup.Cooldown -> cooldown
        SlotGroup.Hotbar -> hotbar
        SlotGroup.ClassResource -> classResource
        SlotGroup.Interface -> interfaceElements
    }

    companion object {
        const val FORMAT = "bdo-tools/slot-layout"
        const val VERSION = 2

        /** Версии, которые сайт умеет читать: в первой все смещения — от центра экрана. */
        val SUPPORTED_VERSIONS = 1..VERSION
    }
}
