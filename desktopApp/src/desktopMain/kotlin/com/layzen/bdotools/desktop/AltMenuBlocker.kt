package com.layzen.bdotools.desktop

import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

/**
 * Не даёт Windows открыть меню окна по одиночному Alt. Иначе после отпускания Alt (тултип навыка) окно уходит
 * в режим меню и перестаёт получать колесо мыши, пока по нему не кликнут.
 *
 * Приложение получает копию события без системного сообщения, поэтому Alt видит как обычно, а оригинал
 * поглощается и до Windows не доходит.
 */
internal object AltMenuBlocker : KeyEventDispatcher {

    /** Включает блокировку для всех окон приложения. */
    fun install() = KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this)

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val target = event.component ?: return false
        if (event.keyCode != KeyEvent.VK_ALT) return false
        val copy = KeyEvent(target, event.id, event.`when`, event.modifiersEx, event.keyCode, event.keyChar, event.keyLocation)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(target, copy)
        event.consume()
        return true
    }
}
