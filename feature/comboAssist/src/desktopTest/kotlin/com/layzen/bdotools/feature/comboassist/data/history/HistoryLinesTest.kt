package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.feature.comboassist.data.history.HistoryLines.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HistoryLinesTest {

    @Test
    fun lineAlreadyOnScreenIsNotNew() {
        val lines = HistoryLines()
        assertEquals(Step.Idle, lines.onFrame(bottom = true, middle = true, nowMs = 0))
        assertEquals(Step.Idle, lines.onFrame(bottom = true, middle = true, nowMs = 50))
    }

    @Test
    fun shiftOpensLineWhileBottomIsStillBlank() {
        val lines = HistoryLines()
        lines.onFrame(bottom = true, middle = true, nowMs = 0)
        assertEquals(Step.Try(shift = true), lines.onFrame(bottom = false, middle = true, nowMs = 50))
        assertIs<Step.Try>(lines.onFrame(bottom = false, middle = true, nowMs = 100)) // иконка ещё не узнана — снова
        lines.recognized()
        assertEquals(Step.Idle, lines.onFrame(bottom = true, middle = true, nowMs = 150)) // текст появился — та же строка
    }

    @Test
    fun repeatOfSameSkillIsAnotherLine() {
        val lines = HistoryLines()
        lines.onFrame(bottom = true, middle = true, nowMs = 0)
        lines.onFrame(bottom = false, middle = true, nowMs = 50); lines.recognized()
        lines.onFrame(bottom = true, middle = true, nowMs = 200)
        assertEquals(Step.Try(shift = true), lines.onFrame(bottom = false, middle = true, nowMs = 400))
    }

    @Test
    fun fadeOutIsNotALineButNextRiseIs() {
        val lines = HistoryLines()
        lines.onFrame(bottom = true, middle = true, nowMs = 0)
        assertEquals(Step.Idle, lines.onFrame(bottom = false, middle = false, nowMs = 2000))
        assertEquals(Step.Try(shift = false), lines.onFrame(bottom = true, middle = false, nowMs = 5000))
    }

    @Test
    fun givesUpAfterTimeout() {
        val lines = HistoryLines(timeoutMs = 300)
        lines.onFrame(bottom = true, middle = true, nowMs = 0)
        lines.onFrame(bottom = false, middle = true, nowMs = 50)
        assertEquals(Step.GaveUp, lines.onFrame(bottom = true, middle = true, nowMs = 400))
        assertEquals(Step.Idle, lines.onFrame(bottom = true, middle = true, nowMs = 450))
    }
}
