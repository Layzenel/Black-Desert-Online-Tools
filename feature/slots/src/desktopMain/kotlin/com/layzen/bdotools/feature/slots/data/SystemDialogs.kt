package com.layzen.bdotools.feature.slots.data

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import java.nio.file.Path
import javax.swing.filechooser.FileSystemView

private val imageExtensions = listOf("png", "jpg", "jpeg", "webp", "bmp", "gif")
private const val LAYOUT_EXTENSION = "json"

/** Системные окна выбора и сохранения файлов (в Windows — стандартный проводник). `null` — окно закрыли. */
internal class SystemDialogs {
    /** Начинает с «Документов»: там лежит папка `Black Desert`. */
    suspend fun pickFolder(): Path? =
        FileKit.openDirectoryPicker(directory = documents())?.file?.toPath()

    suspend fun pickImage(): Path? =
        FileKit.openFilePicker(type = FileKitType.File(imageExtensions))?.file?.toPath()

    suspend fun pickLayout(): Path? =
        FileKit.openFilePicker(type = FileKitType.File(listOf(LAYOUT_EXTENSION)))?.file?.toPath()

    /** Окно «Сохранить как» для файла раскладки; [fileName] — предложенное имя с расширением. */
    suspend fun saveLayout(fileName: String): Path? =
        FileKit.openFileSaver(
            suggestedName = fileName.removeSuffix(".$LAYOUT_EXTENSION"),
            extension = LAYOUT_EXTENSION,
            directory = documents(),
        )?.file?.toPath()

    private fun documents(): PlatformFile = PlatformFile(FileSystemView.getFileSystemView().defaultDirectory)
}
