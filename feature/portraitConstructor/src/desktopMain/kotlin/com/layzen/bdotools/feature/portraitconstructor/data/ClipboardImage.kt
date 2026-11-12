package com.layzen.bdotools.feature.portraitconstructor.data

import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/** Расширения файлов картинок, которые можно вставить из буфера, если в нём скопированный в проводнике файл. */
private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif")

/**
 * Картинка из системного буфера обмена PNG-байтами: скопированное изображение (из браузера, редактора,
 * «Ножниц») или первый файл картинки, скопированный в проводнике. Ничего такого нет — `null`.
 */
internal fun readClipboardImage(): ByteArray? {
    val clipboard = try {
        Toolkit.getDefaultToolkit().systemClipboard
    } catch (e: Exception) {
        return null
    }
    return try {
        when {
            clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor) ->
                (clipboard.getData(DataFlavor.imageFlavor) as? Image)?.let(::toPng)
            clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor) ->
                (clipboard.getData(DataFlavor.javaFileListFlavor) as? List<*>)
                    ?.filterIsInstance<File>()
                    ?.firstOrNull { it.extension.lowercase() in IMAGE_EXTENSIONS }
                    ?.readBytes()
            else -> null
        }
    } catch (e: IOException) {
        null
    } catch (e: IllegalStateException) {
        // буфер занят другой программой
        null
    } catch (e: java.awt.datatransfer.UnsupportedFlavorException) {
        null
    }
}

/** Картинка AWT в PNG; её размеры должны быть уже известны (картинка из буфера загружена). */
private fun toPng(image: Image): ByteArray? {
    val width = image.getWidth(null)
    val height = image.getHeight(null)
    if (width <= 0 || height <= 0) return null
    val buffered = image as? BufferedImage ?: BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also { copy ->
        val graphics = copy.createGraphics()
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()
    }
    return ByteArrayOutputStream().use { out ->
        ImageIO.write(buffered, "png", out)
        out.toByteArray()
    }
}
