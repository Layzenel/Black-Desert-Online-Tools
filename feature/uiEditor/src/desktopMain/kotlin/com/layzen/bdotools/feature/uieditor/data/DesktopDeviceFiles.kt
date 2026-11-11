package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.feature.uieditor.domain.file.DeviceFiles
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/** Файлы с компьютера через системные окна: подложка, импорт и экспорт раскладки. */
internal class DesktopDeviceFiles(private val dialogs: SystemDialogs) : DeviceFiles {

    override suspend fun pickImage(): ByteArray? {
        val file = dialogs.pickImage() ?: return null
        return fileCall { file.readBytes() }
    }

    override suspend fun pickLayoutFile(): String? {
        val file = dialogs.pickLayout() ?: return null
        return fileCall { file.readBytes().decodeToString() }
    }

    /** В настольной версии «скачать» — это окно «Сохранить как»: папки загрузок, как у браузера, нет. */
    override suspend fun download(fileName: String, text: String) {
        val file = dialogs.saveLayout(fileName) ?: return
        fileCall { file.writeBytes(text.encodeToByteArray()) }
    }
}
