package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.feature.comboassist.data.history.HistoryLines.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HistoryIconChangeTest {

    @Test
    fun otherSkillOnTwoFramesIsNewLine() {
        // 2026-11-26: «Абсолют: Удар лодыжкой» после «Мощь: Стальное плечо IV» строкой не замечали
        val change = HistoryIconChange()
        change.recognized(5356)
        assertNull(change.onFrame(5356))
        assertNull(change.onFrame(3035))
        assertEquals(3035, change.onFrame(3035))
        assertNull(change.onFrame(3035)) // та же строка дальше — не новая
    }

    @Test
    fun singleFrameAndUnknownIconsAreIgnored() {
        val change = HistoryIconChange()
        assertNull(change.onFrame(3035)) // пока ни одной строки не узнано — сравнивать не с чем
        assertNull(change.onFrame(3035))
        change.recognized(5356)
        assertNull(change.onFrame(3035))
        assertNull(change.onFrame(null))
        assertNull(change.onFrame(3035))
        assertNull(change.onFrame(5356))
    }

    @Test
    fun lineFoundByIconIsNotOpenedAgainByText() {
        val lines = HistoryLines()
        lines.onFrame(bottom = false, middle = false, nowMs = 0)
        lines.openedByIcon(50)
        assertEquals(Step.Idle, lines.onFrame(bottom = true, middle = false, nowMs = 100))
    }
}
