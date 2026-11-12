package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError

/** Источник нового изображения для портрета. */
interface ImageSource {
    /** Выбор файла на устройстве. `null`, если пользователь закрыл окно выбора. */
    suspend fun pickFromDevice(): ByteArray?

    /** Скачивает изображение по адресу [url]. */
    suspend fun download(url: String): ByteArray

    /** Изображение из буфера обмена; нет его там — [PortraitsError.NoClipboardImage]. */
    suspend fun pasteFromClipboard(): ByteArray
}
