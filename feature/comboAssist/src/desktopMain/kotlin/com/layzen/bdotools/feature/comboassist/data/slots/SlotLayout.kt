package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.core.skill.domain.KeyBindings

/**
 * Слоты перезарядок персонажа, масштаб интерфейса игры и клавиши персонажа.
 *
 * @property slots видимые слоты с умениями
 * @property uiScale «Размер интерфейса» игры (100 % → 1)
 * @property resolutionHeight высота разрешения игры в пикселях
 * @property keys клавиши действий персонажа (`ShortcutKey_Action_Key` его файла)
 * @property historyShown видна ли у персонажа «История испол. навыков» ([parseHistoryShown])
 * @property hotbar видимые слоты хотбара с умениями: они, как и слоты перезарядки, темнеют, когда умение применили
 */
internal data class SlotLayout(
    val slots: List<CooldownSlot>,
    val uiScale: Double,
    val resolutionHeight: Int,
    val keys: KeyBindings = KeyBindings.Default,
    val historyShown: Boolean = true,
    val hotbar: List<CooldownSlot> = emptyList(),
)
