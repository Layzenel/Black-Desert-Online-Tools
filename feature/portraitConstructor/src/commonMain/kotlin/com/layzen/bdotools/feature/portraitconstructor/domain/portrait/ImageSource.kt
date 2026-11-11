package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

/** Источник нового изображения для портрета. */
interface ImageSource {
    /** Выбор файла на устройстве. `null`, если пользователь закрыл окно выбора. */
    suspend fun pickFromDevice(): ByteArray?

    /** Скачивает изображение по адресу [url]. */
    suspend fun download(url: String): ByteArray
}
