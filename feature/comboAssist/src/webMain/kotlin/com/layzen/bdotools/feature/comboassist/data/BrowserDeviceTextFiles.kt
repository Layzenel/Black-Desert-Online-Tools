package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.core.folder.FolderBridge

/** Мост к выбору файла и скачиванию: папка игры ему не нужна, `requiredFolder` — для единообразия с другими мостами. */
private val comboBridge = FolderBridge(
    name = "__bdoCombo",
    requiredFolder = "UserCache",
    script = COMBO_BRIDGE_SOURCE,
    failure = { code -> IllegalStateException(code) },
)

/** Сайт: выбор файла через окно браузера, сохранение — скачивание в папку загрузок. */
internal class BrowserDeviceTextFiles : DeviceTextFiles {
    override suspend fun open(): String? = try {
        comboBridge.call("pickJson")
    } catch (e: IllegalStateException) {
        // CANCELLED — окно закрыли без выбора
        null
    }

    override suspend fun save(fileName: String, text: String) {
        comboBridge.call("download", fileName, text)
    }
}

internal actual fun createDeviceTextFiles(): DeviceTextFiles = BrowserDeviceTextFiles()
