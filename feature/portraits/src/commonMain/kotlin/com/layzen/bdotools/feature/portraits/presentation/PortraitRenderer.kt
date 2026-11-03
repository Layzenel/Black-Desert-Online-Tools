package com.layzen.bdotools.feature.portraits.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.layzen.bdotools.feature.portraits.domain.CellRect
import com.layzen.bdotools.feature.portraits.domain.ImagePlacement
import com.layzen.bdotools.feature.portraits.domain.encodeBmp

/**
 * Нарезает изображение по ячейкам маски. Каждая ячейка — отдельный портрет [CellRect.width] × [CellRect.height],
 * а отступы между ячейками отбрасываются, как и в окне выбора персонажа.
 */
internal object PortraitRenderer {

    /** Фон под прозрачными и непокрытыми участками. В игре портреты всегда непрозрачные. */
    private val background = Color.Black

    fun renderCell(image: ImageBitmap, placement: ImagePlacement, cell: CellRect): ImageBitmap {
        val target = ImageBitmap(cell.width, cell.height)
        val canvas = Canvas(target)
        canvas.drawRect(0f, 0f, cell.width.toFloat(), cell.height.toFloat(), Paint().apply { color = background })

        canvas.save()
        canvas.translate(-cell.x.toFloat(), -cell.y.toFloat())
        canvas.applyPlacement(placement, image)
        canvas.drawImage(image, Offset.Zero, Paint().apply { filterQuality = FilterQuality.High })
        canvas.restore()
        return target
    }
}

/** Переводит систему координат из пикселей маски в пиксели изображения. Тот же порядок, что и в предпросмотре. */
internal fun Canvas.applyPlacement(placement: ImagePlacement, image: ImageBitmap) {
    translate(placement.centerX, placement.centerY)
    rotate(placement.rotationDegrees)
    scale(placement.scale, placement.scale)
    translate(-image.width / 2f, -image.height / 2f)
}

/** Пиксели изображения в формате .bmp из `FaceTexture`. */
internal fun ImageBitmap.toBmp(): ByteArray {
    val pixels = IntArray(width * height)
    readPixels(pixels)
    return encodeBmp(width, height, pixels)
}
