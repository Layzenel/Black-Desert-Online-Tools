package com.layzen.bdotools.feature.uieditor.domain

/** Окно чата, не привязанное к монитору: центр — от ближайшего якоря, остальное — как есть ([ChatWindow]). */
data class AnchoredChat(
    val position: AnchoredPosition,
    val width: Int,
    val height: Int,
    val visible: Boolean = true,
    val combined: Boolean = false,
) {

    /** Окно на экране [screen]; если оно шире, чем там можно, — сужается до предела. */
    fun placeOn(screen: ScreenSpace) =
        ChatWindow(position.placeOn(screen), width, height, visible, combined).fittedTo(screen)

    companion object {
        fun of(chat: ChatWindow, screen: ScreenSpace) =
            AnchoredChat(AnchoredPosition.of(chat.center, screen), chat.width, chat.height, chat.visible, chat.combined)
    }
}
