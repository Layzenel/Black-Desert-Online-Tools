package com.layzen.bdotools.feature.comboassist.data.resource

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ResourceBarPixelsTest {
    private fun image(name: String): BufferedImage = javaClass.getResourceAsStream("/resource/$name")!!.use(ImageIO::read)

    @Test
    fun findsResourceUnderSplitHealth() {
        val shot = image("split-hp.png")
        val lock = assertNotNull(locateResourceBar(shot, UNIT))
        val fill = measureResourceBar(shot, lock)
        assertTrue(lock.rows.first in 38..41, "$lock")
        assertTrue(fill in 195..215, "fill $fill") // 452/490 полоски длиной ≈222 px
    }

    @Test
    fun spentResourceShortensFill() {
        val shot = image("split-hp.png")
        val lock = assertNotNull(locateResourceBar(shot, UNIT))
        val full = measureResourceBar(shot, lock)
        // 40 MP из 490 ушли: конец заливки становится пустой тёмной полоской
        shot.createGraphics().apply {
            color = java.awt.Color(53, 50, 54)
            fillRect(lock.left + full - SPENT, lock.rows.first, SPENT, lock.rows.last - lock.rows.first + 1)
            dispose()
        }
        assertTrue(measureResourceBar(shot, lock) in full - SPENT - 1..full - SPENT + 1)
    }

    @Test
    fun redBuffIconAboveHealthIsNotHealth() {
        // Страйкер, 2026-11-20: над HP красная иконка бафа, а ресурс — оранжевый
        val shot = image("striker.png")
        val lock = assertNotNull(locateResourceBar(shot, UNIT))
        assertTrue(lock.rows.first in 83..85, "$lock")
        assertTrue(measureResourceBar(shot, lock) in 220..232)
    }

    @Test
    fun findsResourceInJoinedPanel() {
        val shot = image("joined-hp.png")
        val lock = assertNotNull(locateResourceBar(shot, UNIT))
        val fill = measureResourceBar(shot, lock)
        assertTrue(fill > 0, "fill $fill")
    }

    private companion object {
        /** Единица интерфейса при 2560×1440 и «Размере интерфейса» 80 %. */
        const val UNIT = 0.8

        /** 40 из 490 на полоске ≈222 px. */
        const val SPENT = 18
    }
}
