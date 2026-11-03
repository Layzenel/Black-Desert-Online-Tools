package com.layzen.bdotools.feature.portraits.data

/** Источник нового изображения для портрета. */
interface ImageSource {
    /** Выбор файла на устройстве. `null`, если пользователь закрыл окно выбора. */
    suspend fun pickFromDevice(): ByteArray?

    suspend fun download(url: String): ByteArray
}
