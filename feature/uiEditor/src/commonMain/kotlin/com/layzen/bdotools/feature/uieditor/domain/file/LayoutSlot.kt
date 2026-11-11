package com.layzen.bdotools.feature.uieditor.domain.file

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Элемент в файле раскладки: номер, как подписано в игре, якорь, смещение центра от якоря и видимость.
 * В файлах первой версии якоря нет — тогда смещение считается от центра экрана.
 * [labelChars] — ширина заглушки надписи в символах ([SlotLabel]); у элементов без надписи поля в файле нет,
 * поэтому файлы с надписями читают и прежние версии сайта.
 * [skill] — `SkillNo` в слоте перезарядки, [memo] — надпись этого навыка; есть, только если файл переносит
 * навыки ([LayoutFile.skills]) и надписи ([LayoutFile.memos]).
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LayoutSlot(
    val slot: Int,
    val anchor: String = "screen-center",
    val x: Int,
    val y: Int,
    val visible: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val labelChars: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val skill: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val memo: String? = null,
)
