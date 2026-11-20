package com.layzen.bdotools.feature.comboassist.data.rage

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RageTimerReaderTest {
    private val buffs = javaClass.getResourceAsStream("/rage/with-rage.png")!!.use(ImageIO::read)
    private val reader = RageTimerReader(ICON)

    @Test
    fun rageBuffsShowStartTimer() {
        val triple = RageBuffMatcher(ICON).search(buffs).triple!!
        assertTrue(triple.all { reader.readsStart(buffs, it) })
    }

    @Test
    fun minutesTimerIsNotStart() {
        // второй ряд того же снимка: бафы предметов с таймером «16 м.»
        val places = listOf(BuffPlace(27, ROW2, 0.0), BuffPlace(54, ROW2, 0.0), BuffPlace(80, ROW2, 0.0))
        places.forEach { assertFalse(reader.readsStart(buffs, it)) }
    }

    @Test
    fun noTimerIsNotStart() {
        val empty = BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().apply { color = Color.DARK_GRAY; fillRect(0, 0, 60, 60); dispose() }
        }
        assertFalse(reader.readsStart(empty, BuffPlace(10, 5, 0.0)))
    }

    private companion object {
        const val ICON = 24
        const val ROW2 = 60
    }
}
