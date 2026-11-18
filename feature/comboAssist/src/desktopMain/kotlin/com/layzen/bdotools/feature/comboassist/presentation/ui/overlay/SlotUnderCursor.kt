package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import java.awt.MouseInfo
import java.awt.Toolkit
import kotlin.math.abs

/**
 * Слот под курсором или `null`. Курсор и размер экрана — в логических координатах Java, слоты — в долях экрана,
 * так масштаб Windows не мешает. Игра на весь основной экран («Полноэкранный в окне»).
 */
internal fun slotUnderCursor(slots: List<ScreenSlot>): ScreenSlot? {
    val pointer = MouseInfo.getPointerInfo()?.location ?: return null
    val screen = Toolkit.getDefaultToolkit().screenSize
    val x = pointer.x.toDouble() / screen.width
    val y = pointer.y.toDouble() / screen.height
    return slots.firstOrNull { slot ->
        val halfY = slot.size / 2
        val halfX = halfY * screen.height / screen.width
        abs(x - slot.centerX) <= halfX && abs(y - slot.centerY) <= halfY
    }
}
