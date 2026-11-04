package com.layzen.bdotools.feature.slots.domain

import kotlinx.serialization.Serializable

/**
 * Элемент в файле раскладки: номер, как подписано в игре, якорь, смещение центра от якоря и видимость.
 * В файлах первой версии якоря нет — тогда смещение считается от центра экрана.
 */
@Serializable
data class LayoutSlot(
    val slot: Int,
    val anchor: String = "screen-center",
    val x: Int,
    val y: Int,
    val visible: Boolean = true,
)
