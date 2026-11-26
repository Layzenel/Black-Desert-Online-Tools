package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.feature.comboassist.data.history.captureScreen
import java.awt.Robot

/**
 * Ряд слотов одного вида — слоты перезарядки или хотбар — во время слежения: какие из них только что ушли на
 * перезарядку ([SlotDarkening]). Ряды снимаются по отдельности: у каждого своя небольшая область экрана.
 *
 * @property slots слоты ряда
 * @property geometry где они на экране
 */
internal class SlotRowWatch private constructor(val slots: List<CooldownSlot>, val geometry: SlotGeometry) {
    private val darkening = SlotDarkening(slots.size)

    /** Новый кадр → слоты ряда, которые только что ушли на перезарядку. */
    fun onFrame(robot: Robot): List<CooldownSlot> {
        val shot = captureScreen(robot, geometry.logicalArea, geometry.area.width, geometry.area.height)
        return darkening.onFrame(geometry.saturations(shot)).map { slots[it] }
    }

    companion object {
        /** Слежение за слотами [slots] при масштабе интерфейса раскладки [layout] или `null`, если слотов нет. */
        fun of(layout: SlotLayout, slots: List<CooldownSlot>): SlotRowWatch? =
            SlotGeometry.forScreen(layout.copy(slots = slots))?.let { SlotRowWatch(slots, it) }
    }
}
