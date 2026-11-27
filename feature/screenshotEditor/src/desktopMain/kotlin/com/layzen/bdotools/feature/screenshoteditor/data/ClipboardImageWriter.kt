package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/** Кладёт картинку PNG [png] в системный буфер обмена; буфер занят или недоступен — [ScreenshotError.Clipboard]. */
internal fun writeClipboardImage(png: ByteArray) {
    val image = ImageIO.read(ByteArrayInputStream(png)) ?: throw ScreenshotException(ScreenshotError.Clipboard, "not an image")
    try {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(ImageSelection(image), null)
    } catch (e: IllegalStateException) {
        throw ScreenshotException(ScreenshotError.Clipboard, e.message)
    }
}

/** Картинка для буфера обмена AWT: у JDK нет готового [Transferable] для изображений. */
private class ImageSelection(private val image: Image) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.imageFlavor

    override fun getTransferData(flavor: DataFlavor): Any =
        if (isDataFlavorSupported(flavor)) image else throw UnsupportedFlavorException(flavor)
}
