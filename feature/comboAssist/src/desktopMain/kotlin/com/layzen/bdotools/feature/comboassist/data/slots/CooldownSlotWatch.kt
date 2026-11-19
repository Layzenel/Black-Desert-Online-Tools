package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.feature.comboassist.data.history.IconMatcher
import com.layzen.bdotools.feature.comboassist.data.history.WatchLog
import com.layzen.bdotools.feature.comboassist.data.history.captureScreen
import com.layzen.bdotools.composable.overlay.isCursorShown
import java.awt.MouseInfo
import java.awt.Robot

/**
 * Слоты перезарядок во время слежения: какие умения только что ушли на перезарядку. Раскладка — того персонажа,
 * которого видно на экране ([PlayedCharacterDetector]), а не угаданного по файлам: у двух персонажей одного класса
 * слоты бывают разложены по-разному, и чужая раскладка выдала бы не то умение. Персонаж проверяется раз в
 * [DETECT_MS]; пока он не узнан, слоты молчат. Пока курсор показан над слотами, слоты тоже молчат.
 *
 * @property layouts слоты всех персонажей аккаунта
 * @property matcher иконки умений для узнавания персонажа
 * @property log журнал слежения
 */
internal class CooldownSlotWatch(
    private val layouts: List<SlotLayout>,
    private val matcher: IconMatcher,
    private val log: WatchLog,
) {
    private var layout: SlotLayout? = null
    private var geometry: SlotGeometry? = null
    private var darkening: SlotDarkening? = null
    private var detectedAt = -DETECT_MS

    /** Кадр в момент [nowMs] → умения, чьи слоты только что ушли на перезарядку. */
    fun onFrame(robot: Robot, nowMs: Long): List<Int> {
        if (nowMs - detectedAt >= DETECT_MS) {
            detectedAt = nowMs
            detect(robot, nowMs)
        }
        val current = layout ?: return emptyList()
        val slots = geometry ?: return emptyList()
        val shot = captureScreen(robot, slots.logicalArea, slots.area.width, slots.area.height)
        val gone = darkening?.onFrame(slots.saturations(shot)).orEmpty()
        if (gone.isNotEmpty() && pointerOverSlots(slots)) {
            gone.forEach { log.line(nowMs, "slot ${current.slots[it].skillNo} darkened under the cursor, ignored") }
            return emptyList()
        }
        return gone.map { current.slots[it].skillNo }
    }

    /**
     * Курсор показан и стоит над слотами: тултипы и подсказки у курсора закрывают соседние слоты, а снимок экрана видит
     * их вместе с игрой — потемневший слот тогда не значит, что умение применили. В бою игра курсор прячет.
     */
    private fun pointerOverSlots(slots: SlotGeometry): Boolean {
        if (!isCursorShown()) return false
        val pointer = MouseInfo.getPointerInfo()?.location ?: return false
        return slots.logicalArea.contains(pointer)
    }

    private fun detect(robot: Robot, nowMs: Long) {
        val found = PlayedCharacterDetector.detect(robot, layouts, matcher) ?: return
        if (found == layout) return
        layout = found
        geometry = SlotGeometry.forScreen(found)
        darkening = SlotDarkening(found.slots.size)
        log.line(nowMs, "cooldown slots of the played character: ${found.slots.map { it.skillNo }}")
    }

    private companion object {
        /** Как часто проверять, каким персонажем играют. */
        const val DETECT_MS = 5000L
    }
}
