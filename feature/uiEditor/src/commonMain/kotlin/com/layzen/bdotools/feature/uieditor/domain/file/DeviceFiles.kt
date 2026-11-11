package com.layzen.bdotools.feature.uieditor.domain.file

/** Файлы с устройства пользователя вне папки игры: выбор и скачивание. */
interface DeviceFiles {
    /** Окно выбора картинки; `null`, если пользователь его закрыл. */
    suspend fun pickImage(): ByteArray?

    /** Окно выбора файла `.json`; `null`, если пользователь его закрыл. */
    suspend fun pickLayoutFile(): String?

    /** Скачивает [text] как файл [fileName] в папку загрузок браузера. */
    suspend fun download(fileName: String, text: String)
}
