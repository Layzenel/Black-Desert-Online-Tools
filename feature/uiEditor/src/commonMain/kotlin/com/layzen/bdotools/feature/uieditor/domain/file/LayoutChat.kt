package com.layzen.bdotools.feature.uieditor.domain.file

import kotlinx.serialization.Serializable

/**
 * Окно чата в файле раскладки: [window] — `WindowIndex` в игре, центр — смещение [x], [y] от якоря [anchor],
 * как у [LayoutSlot]; [width] и [height] — размер в UI-px,
 * [visible] — глазик окна, [combined] — вкладка главного окна, а не отдельное окно.
 */
@Serializable
data class LayoutChat(
    val window: Int,
    val anchor: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val visible: Boolean = true,
    val combined: Boolean = false,
)
