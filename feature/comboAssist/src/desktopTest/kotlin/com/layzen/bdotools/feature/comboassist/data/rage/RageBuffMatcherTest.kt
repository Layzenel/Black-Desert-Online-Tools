package com.layzen.bdotools.feature.comboassist.data.rage

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RageBuffMatcherTest {
    private fun resource(path: String): BufferedImage = javaClass.getResourceAsStream(path)!!.use(ImageIO::read)

    /** Серый ряд с иконками бафов: номер эталона (1–3) → левый край. */
    private fun row(vararg icons: Pair<Int, Int>): BufferedImage =
        BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().apply {
                color = Color.GRAY
                fillRect(0, 0, WIDTH, HEIGHT)
                icons.forEach { (buff, x) -> drawImage(resource("/rage/buff$buff.png"), x, TOP, null) }
                dispose()
            }
        }

    private fun triple(image: BufferedImage) = RageBuffMatcher(ICON).search(image).triple != null

    @Test
    fun rageBuffsAreFoundInBuffList() {
        assertTrue(triple(resource("/rage/with-rage.png")))
    }

    @Test
    fun otherBuffsAreNotRage() {
        assertFalse(triple(resource("/rage/without-rage.png")))
    }

    @Test
    fun threeIconsInARowAreRage() {
        assertTrue(triple(row(1 to 10, 2 to 36, 3 to 62)))
    }

    @Test
    fun lookalikeIconsApartAreNotRage() {
        // у обычных бафов умений бывают те же иконки, что у 1 и 3, — но не подряд и без второй
        assertFalse(triple(row(1 to 10, 3 to 36)))
        assertFalse(triple(row(1 to 10, 2 to 36, 3 to 120)))
        assertFalse(triple(row(3 to 10, 2 to 36, 1 to 62)))
    }

    private companion object {
        /** Иконка на снимке 2560×1440 при «Размере интерфейса» 80 %. */
        const val ICON = 24
        const val WIDTH = 200
        const val HEIGHT = 50
        const val TOP = 10
    }
}
