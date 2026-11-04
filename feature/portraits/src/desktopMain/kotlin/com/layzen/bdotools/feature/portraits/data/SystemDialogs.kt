package com.layzen.bdotools.feature.portraits.data

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import java.nio.file.Path
import javax.swing.filechooser.FileSystemView

private val imageExtensions = listOf("png", "jpg", "jpeg", "webp", "bmp", "gif")

/** Системные окна выбора папки и картинки (в Windows — стандартный проводник). `null` — окно закрыли. */
internal class SystemDialogs {
    /** Начинает с «Документов»: там лежит папка `Black Desert`. */
    suspend fun pickFolder(): Path? =
        FileKit.openDirectoryPicker(directory = documents())?.file?.toPath()

    suspend fun pickImage(): Path? =
        FileKit.openFilePicker(type = FileKitType.File(imageExtensions))?.file?.toPath()

    private fun documents(): PlatformFile = PlatformFile(FileSystemView.getFileSystemView().defaultDirectory)
}
