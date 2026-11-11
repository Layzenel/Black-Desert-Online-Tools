package com.layzen.bdotools.feature.portraitconstructor.presentation.render

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsException
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.StoredPortrait
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Работа с картинками портретов: декодирование файлов и отрисовка надписи и значков.
 * Отрисовщик создаётся в композиции (ему нужны шрифты и SVG) и передаётся сюда через [useDecorator].
 */
internal class PortraitImages {

    private val decorator = MutableStateFlow<PortraitDecorator?>(null)

    /** Подставляет отрисовщик украшений, когда его создала композиция. */
    fun useDecorator(value: PortraitDecorator) {
        decorator.value = value
    }

    /** Декодирует PNG, JPG, WEBP, BMP или GIF. Неподдерживаемый файл — [PortraitsError.NotImage]. */
    fun decode(bytes: ByteArray): ImageBitmap =
        try {
            bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            throw PortraitsException(PortraitsError.NotImage, e.message)
        }

    /** Готовый портрет из сохранённых чистой основы и украшения. */
    suspend fun render(stored: StoredPortrait): ImageBitmap = decorate(decode(stored.clean), stored.decoration)

    /** Рисует украшения поверх чистого портрета. Ждёт, пока загрузятся SVG-значки. */
    suspend fun decorate(clean: ImageBitmap, decoration: PortraitDecoration): ImageBitmap {
        if (decoration.isEmpty) return clean
        val ready = decorator.filterNotNull().first { it.isReady }
        return ready.decorate(clean, decoration)
    }
}
