package com.layzen.bdotools.feature.comboassist.data

/** Текстовые файлы с устройства через окна платформы. `null` и ничего не сделано — окно закрыли. */
interface DeviceTextFiles {
    /** Окно выбора файла `.json` → его текст. */
    suspend fun open(): String?

    /** Сохраняет [text] в файл с предложенным именем [fileName]: в настольной версии — «Сохранить как», на сайте — скачивание. */
    suspend fun save(fileName: String, text: String)
}

/** Файлы устройства своей платформы. */
internal expect fun createDeviceTextFiles(): DeviceTextFiles
