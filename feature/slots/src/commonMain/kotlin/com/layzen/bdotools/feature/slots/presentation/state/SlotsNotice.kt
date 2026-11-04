package com.layzen.bdotools.feature.slots.presentation.state

import com.layzen.bdotools.feature.slots.data.SlotsError

/** Всплывающее сообщение о результате действия. */
sealed interface SlotsNotice {
    val isError: Boolean get() = false

    data class Applied(val backupPath: String) : SlotsNotice

    data class PresetSaved(val name: String) : SlotsNotice

    data class PresetDeleted(val name: String) : SlotsNotice

    /**
     * Раскладка загружена в редактор. [offScreen] — сколько элементов не влезло на этот экран;
     * [suggestedUiScale] — масштаб интерфейса, при котором влезет всё, если такой есть.
     */
    data class Imported(val name: String, val offScreen: Int = 0, val suggestedUiScale: Double? = null) : SlotsNotice {
        override val isError: Boolean get() = offScreen > 0
    }

    data class Failed(val error: SlotsError) : SlotsNotice {
        override val isError: Boolean get() = true
    }
}
