package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.uikit.icons.title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SlotMarksTest {
    private val marks = SlotMarks(Rect(0f, 0f, 100f, 100f), listOf(SkillProtection.Invincible), listOf(CrowdControl.Stun, CrowdControl.Bound))

    @Test
    fun findsShieldAndControlUnderPoint() {
        val shield = marks.shieldRects().single().second
        assertEquals(SkillProtection.Invincible.title, marks.titleAt(shield.center))
        val (_, bound) = marks.controlRects().last()
        assertEquals(CrowdControl.Bound.title, marks.titleAt(bound.center))
        assertNull(marks.titleAt(Offset(50f, 50f))) // середина иконки — не значок
    }
}
