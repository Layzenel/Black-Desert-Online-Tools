package com.layzen.bdotools.feature.slots.domain

import kotlinx.serialization.Serializable

/**
 * Файл раскладки `*.json` — то, что экспортируется, импортируется и лежит в папке пресетов.
 *
 * Координаты — центр слота от центра экрана в UI-px ([origin] и [units] записаны в файл, чтобы
 * человек, открывший его, понял, что значат числа). Группы, которых нет в файле, при импорте не меняются.
 */
@Serializable
data class LayoutFile(
    val format: String = FORMAT,
    val version: Int = VERSION,
    val name: String,
    val origin: String = "screen-center",
    val units: String = "ui-px",
    val cooldown: List<LayoutSlot> = emptyList(),
    val hotbar: List<LayoutSlot> = emptyList(),
) {
    companion object {
        const val FORMAT = "bdo-tools/slot-layout"
        const val VERSION = 1
    }
}
