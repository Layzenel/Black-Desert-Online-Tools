package com.layzen.bdotools.feature.comboassist.data.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResourceDropsTest {
    private fun drops(vararg fills: Pair<Long, Int>) = ResourceDrops(minDrop = 3).apply { fills.forEach { (t, f) -> onFill(t, f) } }

    @Test
    fun stepCountsWhenSkillActuallyStarts() {
        // клавиши умения 2 зажаты на 100 мс, во время анимации умения 1; само умение 2 вышло на 900 мс
        val drops = drops(0L to 200, 50L to 200, 100L to 200, 850L to 200, 900L to 180, 950L to 180)
        assertEquals(900L, drops.claim(100))
    }

    @Test
    fun oneDropIsOneStep() {
        // умение 1 нажато на 0 и вышло на 50, клавиши умения 2 зажаты на 100 — второго проседания ещё нет
        val drops = drops(0L to 200, 50L to 180, 100L to 180, 150L to 180)
        assertEquals(50L, drops.claim(0))
        assertNull(drops.claim(100))
    }

    @Test
    fun resourceSpentOverTwoFramesIsOneDrop() {
        val drops = drops(0L to 200, 50L to 190, 100L to 180, 150L to 180)
        assertEquals(50L, drops.claim(0))
        assertNull(drops.claim(0))
    }

    @Test
    fun onePixelSpendThatStaysCounts() {
        // связка Страйкера тратит 5 из ~850 — пиксель-другой полоски
        val drops = ResourceDrops(minDrop = 1).apply { onFill(0, 229); onFill(50, 229); onFill(100, 228); onFill(150, 228) }
        assertEquals(100L, drops.claim(60))
    }

    @Test
    fun spendRefilledNextFrameStillCounts() {
        // Страйкер: умение тратит ресурс и тут же восполняет его ударами
        val drops = ResourceDrops(minDrop = 1).apply { onFill(0, 229); onFill(50, 229); onFill(100, 224); onFill(150, 230) }
        assertEquals(100L, drops.claim(60))
    }

    @Test
    fun heldKeyTakesOnlyFreshDrops() {
        // умения подряд на зажатой клавише: каждое проседание — своё; старое проседание (филлер) не берётся
        val drops = drops(0L to 200, 50L to 180, 600L to 180, 650L to 160, 700L to 160)
        assertEquals(650L, drops.claimFresh(700))
        assertNull(drops.claimFresh(700))
        assertNull(drops.claimFresh(1000))
    }

    @Test
    fun chainSkipsDropRightAfterPreviousStep() {
        // второй расход того же умения через 0,17 с — не следующее умение цепочки
        val drops = drops(0L to 200, 50L to 180, 220L to 170, 270L to 170)
        drops.claim(0)
        assertNull(drops.claimFresh(270, notBefore = 50 + 400))
    }

    @Test
    fun ignoredPressSpendsNothing() {
        val drops = drops(0L to 200, 50L to 201, 100L to 202)
        assertNull(drops.claim(40))
    }

    @Test
    fun dropSeenOneFrameBeforePressStillCounts() {
        // ресурс ушёл между кадрами, а нажатие заметили только на следующем кадре
        val drops = drops(0L to 200, 50L to 182, 100L to 182)
        assertEquals(50L, drops.claim(100))
    }
}
