package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.feature.comboassist.data.history.IconMatcher
import com.layzen.bdotools.feature.comboassist.data.history.captureScreen
import java.awt.Robot

/**
 * Каким персонажем играют — по слотам перезарядок на экране. У каждого персонажа в файле свои умения в слотах,
 * а места слотов общие для аккаунта: иконка в каждом слоте сравнивается с иконкой умения, которое стоит там
 * у персонажа ([IconMatcher.difference]), и выигрывает персонаж с самой малой средней разницей. Слоты на
 * перезарядке темнее иконки у всех одинаково, поэтому выбор остаётся верным. Если отрыв от второго мал — `null`.
 */
internal object PlayedCharacterDetector {
    /** Меньше слотов — слишком мало, чтобы отличить персонажа. */
    private const val MIN_SLOTS = 3

    /** Насколько лучший персонаж должен быть похожее второго. */
    private const val MIN_GAP = 5.0

    /** Слоты персонажа из [layouts], который сейчас на экране, или `null`, если не понять. */
    fun detect(robot: Robot, layouts: List<SlotLayout>, matcher: IconMatcher): SlotLayout? {
        val first = layouts.firstOrNull() ?: return null
        val union = SlotLayout(layouts.flatMap { it.slots }.distinctBy { it.centerX to it.centerY }, first.uiScale, first.resolutionHeight)
        val geometry = SlotGeometry.forScreen(union) ?: return null
        val shot = captureScreen(robot, geometry.logicalArea, geometry.area.width, geometry.area.height)
        val scores = layouts.mapNotNull { layout ->
            val differences = layout.slots.mapNotNull { matcher.difference(geometry.crop(shot, it.centerX, it.centerY), it.skillNo) }
            if (differences.size < MIN_SLOTS) null else layout to differences.average()
        }.sortedBy { it.second }
        val best = scores.firstOrNull() ?: return null
        val second = scores.getOrNull(1)?.second ?: Double.MAX_VALUE
        // у двух персонажей с одинаковыми слотами (одного класса) отрыва нет, но и путать их не страшно
        val same = scores.getOrNull(1)?.first?.slots == best.first.slots
        return best.first.takeIf { same || second - best.second >= MIN_GAP }
    }
}
