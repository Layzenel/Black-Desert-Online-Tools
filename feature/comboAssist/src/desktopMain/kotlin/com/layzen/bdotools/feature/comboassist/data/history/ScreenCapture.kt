package com.layzen.bdotools.feature.comboassist.data.history

import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage

/**
 * Снимок области [logical] (логические координаты Java) в настоящих пикселях [width]×[height]: Robot берёт
 * логические координаты, лучший вариант снимка — настоящий, без уменьшения от масштаба Windows.
 */
internal fun captureScreen(robot: Robot, logical: Rectangle, width: Int, height: Int): BufferedImage {
    val variants = robot.createMultiResolutionScreenCapture(logical).resolutionVariants
    val best = variants.maxBy { it.getWidth(null) }
    if (best is BufferedImage && best.width == width && best.height == height) return best
    return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).also { image ->
        image.createGraphics().apply { drawImage(best, 0, 0, width, height, null); dispose() }
    }
}
