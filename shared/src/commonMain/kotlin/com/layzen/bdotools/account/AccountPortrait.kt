package com.layzen.bdotools.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.decodeToImageBitmap

/** Маленький портрет персонажа из `FaceTexture`; пока он читается или если файла нет — пустая ячейка. */
@Composable
internal fun AccountPortrait(characterId: String, readPortrait: suspend (String) -> ByteArray?) {
    val image by produceState<ImageBitmap?>(null, characterId) { value = loadImage(characterId, readPortrait) }
    Box(
        Modifier
            .size(AccountDimens.portraitWidth, AccountDimens.portraitHeight)
            .background(AppTheme.colors.backgroundInput),
    ) {
        image?.let { Image(it, contentDescription = null, Modifier.matchParentSize(), contentScale = ContentScale.Crop) }
    }
}

// Превью необязательно: битый файл или ошибка чтения оставляют ячейку пустой
private suspend fun loadImage(characterId: String, readPortrait: suspend (String) -> ByteArray?): ImageBitmap? =
    try {
        readPortrait(characterId)?.decodeToImageBitmap()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
