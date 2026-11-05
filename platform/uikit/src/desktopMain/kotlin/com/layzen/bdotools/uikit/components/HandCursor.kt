package com.layzen.bdotools.uikit.components

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Cursor
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.Toolkit
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage

private const val CURSOR_SIZE = 32
private const val OUTLINE_WIDTH = 1.6f
private const val THUMB_ANGLE_DEGREES = -35.0
private val HOTSPOT = Point(CURSOR_SIZE / 2, CURSOR_SIZE / 2)

/** Курсор-рука 32×32: белый силуэт с чёрным контуром; [closed] — сжатая в кулак, иначе открытая ладонь. */
internal fun handCursor(name: String, closed: Boolean): Cursor {
    val image = BufferedImage(CURSOR_SIZE, CURSOR_SIZE, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val silhouette = Area().apply { (if (closed) fist() else openPalm()).forEach { add(Area(it)) } }
    graphics.color = Color.WHITE
    graphics.fill(silhouette)
    graphics.color = Color.BLACK
    graphics.stroke = BasicStroke(OUTLINE_WIDTH)
    graphics.draw(silhouette)
    graphics.dispose()
    return Toolkit.getDefaultToolkit().createCustomCursor(image, HOTSPOT, name)
}

private fun roundRect(x: Double, y: Double, width: Double, height: Double): Shape =
    RoundRectangle2D.Double(x, y, width, height, minOf(width, height), minOf(width, height))

private fun openPalm(): List<Shape> = listOf(
    roundRect(9.0, 14.0, 15.0, 13.0),
    roundRect(9.5, 6.0, 3.6, 12.0),
    roundRect(13.5, 4.0, 3.6, 14.0),
    roundRect(17.5, 5.0, 3.6, 13.0),
    roundRect(21.4, 8.0, 3.6, 11.0),
    AffineTransform.getRotateInstance(Math.toRadians(THUMB_ANGLE_DEGREES), 8.0, 20.0)
        .createTransformedShape(roundRect(6.0, 14.0, 3.8, 10.0)),
)

private fun fist(): List<Shape> = listOf(
    roundRect(8.0, 13.0, 17.0, 13.0),
    roundRect(8.5, 10.0, 4.2, 8.0),
    roundRect(12.6, 9.0, 4.2, 9.0),
    roundRect(16.7, 9.5, 4.2, 8.5),
    roundRect(20.8, 11.0, 4.2, 7.0),
    roundRect(10.0, 19.0, 11.0, 6.0),
)
