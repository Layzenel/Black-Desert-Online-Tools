package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Слоты перезарядок персонажа и масштаб интерфейса игры.
 *
 * @property slots видимые слоты с умениями
 * @property uiScale «Размер интерфейса» игры (100 % → 1)
 * @property resolutionHeight высота разрешения игры в пикселях
 */
internal data class SlotLayout(val slots: List<CooldownSlot>, val uiScale: Double, val resolutionHeight: Int)
