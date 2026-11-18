package com.layzen.bdotools.core.skill.domain

private const val F1 = 112
private const val NUMPAD_0 = 96
private val NAMED_KEYS = mapOf(
    8 to "Backspace", 9 to "Tab", 13 to "Enter", 16 to "Shift", 17 to "Ctrl", 18 to "Alt", 20 to "Caps Lock",
    27 to "Esc", 37 to "Left", 38 to "Up", 39 to "Right", 40 to "Down", 192 to "~",
)

/** Коды кнопок мыши и пробела: их подпись зависит от языка интерфейса и берётся из ресурсов. */
object TranslatedKeys {
    const val LEFT_MOUSE = 1
    const val RIGHT_MOUSE = 2
    const val MIDDLE_MOUSE = 4
    const val SPACE = 32
}

/** Подпись клавиши по виртуальному коду Windows; такие клавиши одинаковы на всех языках. */
fun keyName(keyCode: Int): String = when (keyCode) {
    in 'A'.code..'Z'.code, in '0'.code..'9'.code -> keyCode.toChar().toString()
    in F1..F1 + 11 -> "F${keyCode - F1 + 1}"
    in NUMPAD_0..NUMPAD_0 + 9 -> "Num ${keyCode - NUMPAD_0}"
    else -> NAMED_KEYS[keyCode] ?: "#$keyCode"
}
