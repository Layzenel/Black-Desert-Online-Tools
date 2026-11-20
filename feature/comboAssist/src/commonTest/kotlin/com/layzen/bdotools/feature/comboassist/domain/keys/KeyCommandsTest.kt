package com.layzen.bdotools.feature.comboassist.domain.keys

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeyCommandsTest {
    private val boneBreaker = "<span style=\"color: #d8ff00\">{KeyBind:Jump}</span> after Combo: Crushing Descent\n" +
        "<span style=\"color: #d8ff00\">{KeyBind:Attack1} or {KeyBind:Attack2}</span> during Unshakable Will II"

    @Test
    fun linesAndAlternativesAreVariants() {
        assertEquals(listOf(setOf("Jump"), setOf("Attack1"), setOf("Attack2")), keyVariants(boneBreaker))
    }

    @Test
    fun holdToDoSomethingIsNotStart() {
        // 7984 «Мощь: Ливень смерти III» Агента: начинается Shift + ПКМ, а ПКМ дальше — стрельба во время умения
        val command = "{KeyBind:Dash} + {KeyBind:Attack2}\nHold {KeyBind:Attack2} to fire continuously"
        assertEquals(listOf(setOf("Dash", "Attack2")), keyVariants(command))
        assertEquals(listOf(setOf("Attack1")), keyVariants("Hold {KeyBind:Attack1} to charge"))
    }

    @Test
    fun plusMeansHeldTogether() {
        assertEquals(listOf(setOf("MoveFront", "Attack1")), keyVariants("{KeyBind:MoveFront} + {KeyBind:Attack1}\nPassive"))
    }

    @Test
    fun stepExpectedNowWinsOverNextOne() {
        val crushing = KeyStep(9707, null, listOf(setOf("Attack1"), setOf("Attack2")))
        val bone = KeyStep(9708, 9707, keyVariants(boneBreaker))
        assertEquals(crushing, matchKeyStep(listOf(crushing, bone), "Attack1", setOf("Attack1")))
        assertEquals(bone, matchKeyStep(listOf(crushing, bone), "Jump", setOf("Jump")))
    }

    @Test
    fun allKeysOfVariantMustBeHeld() {
        val deepCut = KeyStep(9735, null, listOf(setOf("MoveFront", "Attack1")))
        assertNull(matchKeyStep(listOf(deepCut), "Attack1", setOf("Attack1")))
        assertEquals(deepCut, matchKeyStep(listOf(deepCut), "Attack1", setOf("MoveFront", "Attack1")))
    }

    @Test
    fun heldCommandMatchesStepExpectedNow() {
        // держат ЛКМ — игра применяет умения на ЛКМ подряд; продолжение «после» по зажатой клавише не засчитывается
        val second = KeyStep(2, null, listOf(setOf("Attack1")))
        val later = KeyStep(3, 2, listOf(setOf("Attack1")))
        assertEquals(second, matchHeldStep(listOf(second, later), setOf("Attack1")))
        assertNull(matchHeldStep(listOf(second), setOf("Attack2")))
        assertNull(matchHeldStep(listOf(KeyStep(4, null, listOf(setOf("MoveFront", "Attack1")))), setOf("Attack1")))
    }

    @Test
    fun moreSpecificVariantWinsAndEqualOnesAreAmbiguous() {
        val plain = KeyStep(1, null, listOf(setOf("Attack1")))
        val forward = KeyStep(2, null, listOf(setOf("MoveFront", "Attack1")))
        assertEquals(forward, matchKeyStep(listOf(plain, forward), "Attack1", setOf("MoveFront", "Attack1")))
        assertNull(matchKeyStep(listOf(plain, KeyStep(3, null, listOf(setOf("Attack1")))), "Attack1", setOf("Attack1")))
    }
}
