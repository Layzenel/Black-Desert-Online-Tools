package com.layzen.bdotools.feature.comboassist.data

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.filechooser.FileSystemView

/** Окна Windows «Открыть» и «Сохранить как»; начинают с «Документов». */
internal class DesktopDeviceTextFiles : DeviceTextFiles {
    override suspend fun open(): String? {
        val file = FileKit.openFilePicker(type = FileKitType.File(listOf(EXTENSION)), directory = documents())?.file ?: return null
        return withContext(Dispatchers.IO) { file.readText() }
    }

    override suspend fun save(fileName: String, text: String) {
        val file = FileKit.openFileSaver(suggestedName = fileName.removeSuffix(".$EXTENSION"), extension = EXTENSION, directory = documents())?.file
            ?: return
        withContext(Dispatchers.IO) { file.writeText(text) }
    }

    private fun documents(): PlatformFile = PlatformFile(FileSystemView.getFileSystemView().defaultDirectory)

    private companion object {
        const val EXTENSION = "json"
    }
}

internal actual fun createDeviceTextFiles(): DeviceTextFiles = DesktopDeviceTextFiles()
