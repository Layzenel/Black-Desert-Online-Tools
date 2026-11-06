package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.feature.portraitconstructor.domain.PortraitDecoration

/**
 * Что записать для одного персонажа.
 * [face] уходит в `FaceTexture`. Если [decoration] не пустое, рядом сохраняются [cleanBase] — портрет без
 * украшений — и само украшение, чтобы его можно было поменять позже. Пустое украшение удаляет оба файла.
 */
class PortraitWrite(
    val characterId: String,
    val face: ByteArray,
    val cleanBase: ByteArray,
    val decoration: PortraitDecoration,
)
