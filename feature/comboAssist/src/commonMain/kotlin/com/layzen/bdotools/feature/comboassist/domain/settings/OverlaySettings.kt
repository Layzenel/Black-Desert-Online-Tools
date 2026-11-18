package com.layzen.bdotools.feature.comboassist.domain.settings

/**
 * Настройки окон поверх игры.
 *
 * @property slotTooltips показывать тултип умения, когда курсор задержался над слотом перезарядки
 * @property comboScale размер оверлея комбо в процентах от обычного
 * @property comboOpacity непрозрачность оверлея комбо в процентах: 100 — непрозрачный
 */
data class OverlaySettings(
    val slotTooltips: Boolean = true,
    val comboScale: Int = 100,
    val comboOpacity: Int = 100,
)
