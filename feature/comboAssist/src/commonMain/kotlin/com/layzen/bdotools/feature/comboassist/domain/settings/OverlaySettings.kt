package com.layzen.bdotools.feature.comboassist.domain.settings

/**
 * Настройки окон поверх игры.
 *
 * @property slotTooltips показывать тултип умения, когда курсор задержался над слотом перезарядки
 * @property comboScale размер оверлея комбо в процентах от обычного
 * @property comboOpacity непрозрачность оверлея комбо в процентах: 100 — непрозрачный
 * @property schemaShown вместе с трекером показывать отдельное окно со схемой комбо
 * @property slotShields показывать на слотах перезарядки щиты защиты умения в PvP: устойчивость, защита спереди, неуязвимость
 * @property slotControls показывать под иконкой слота перезарядки значки контролей умения в PvP
 * @property trackerNoticeSeen игрок уже прочитал, как работает трекер: окно с объяснением при первом открытии больше не показывается
 */
data class OverlaySettings(
    val slotTooltips: Boolean = true,
    val comboScale: Int = 100,
    val comboOpacity: Int = 100,
    val schemaShown: Boolean = false,
    val slotShields: Boolean = false,
    val slotControls: Boolean = false,
    val trackerNoticeSeen: Boolean = false,
)
