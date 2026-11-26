package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.feature.comboassist.data.history.IconMatcher
import com.layzen.bdotools.feature.comboassist.data.history.WatchLog
import com.layzen.bdotools.composable.overlay.isCursorShown
import java.awt.MouseInfo
import java.awt.Robot

/**
 * Слоты перезарядок и хотбара во время слежения: какие умения только что ушли на перезарядку. Раскладка — того
 * персонажа, которым играют ([PlayedLayout]): у двух персонажей одного класса слоты бывают разложены по-разному, и
 * чужая раскладка выдала бы не то умение. Персонаж проверяется раз в [DETECT_MS]; пока он не узнан или все его слоты
 * скрыты, слоты молчат. Пока курсор показан над слотами, слоты тоже молчат.
 *
 * С хотбара следятся только умения, которых нет на видимых слотах перезарядки: одно умение в обоих рядах темнеет
 * дважды, а слот перезарядки точнее (на хотбаре бывают и предметы).
 *
 * @property layouts слоты всех персонажей аккаунта по `CharacterNo`
 * @property matcher иконки умений для узнавания персонажа
 * @property log журнал слежения
 */
internal class CooldownSlotWatch(
    private val layouts: Map<String, SlotLayout>,
    private val matcher: IconMatcher,
    private val log: WatchLog,
) {
    private var layout: SlotLayout? = null
    private var rows: List<SlotRowWatch> = emptyList()
    private var detectedAt = -DETECT_MS

    /** Кадр в момент [nowMs] → умения, чьи слоты только что ушли на перезарядку. */
    fun onFrame(robot: Robot, nowMs: Long): List<Int> {
        if (nowMs - detectedAt >= DETECT_MS) {
            detectedAt = nowMs
            detect(robot, nowMs)
        }
        val gone = rows.associateWith { it.onFrame(robot) }.filterValues { it.isNotEmpty() }
        val (ignored, used) = gone.entries.partition { (row, _) -> pointerOver(row.geometry) }
        ignored.flatMap { it.value }.forEach { log.line(nowMs, "slot ${it.skillNo} darkened under the cursor, ignored") }
        return used.flatMap { it.value }.map { it.skillNo }
    }

    /**
     * Курсор показан и стоит над рядом слотов: тултипы и подсказки у курсора закрывают соседние слоты, а снимок экрана
     * видит их вместе с игрой — потемневший слот тогда не значит, что умение применили. В бою игра курсор прячет.
     */
    private fun pointerOver(slots: SlotGeometry): Boolean {
        if (!isCursorShown()) return false
        val pointer = MouseInfo.getPointerInfo()?.location ?: return false
        return slots.logicalArea.contains(pointer)
    }

    private fun detect(robot: Robot, nowMs: Long) {
        val found = PlayedLayout.find(robot, layouts, matcher) ?: return
        if (found == layout) return
        layout = found
        val onSlots = found.slots.map { it.skillNo }.toSet()
        val hotbar = found.hotbar.filter { it.skillNo !in onSlots }
        rows = listOfNotNull(SlotRowWatch.of(found, found.slots), SlotRowWatch.of(found, hotbar))
        log.line(nowMs, "played character: cooldown slots ${found.slots.map { it.skillNo }}, hotbar ${hotbar.map { it.skillNo }}")
    }

    private companion object {
        /** Как часто проверять, каким персонажем играют. */
        const val DETECT_MS = 5000L
    }
}
