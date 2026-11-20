package com.layzen.bdotools.feature.comboassist.data.keys

/** `RI_KEY_BREAK`: клавишу отпустили. */
private const val KEY_BREAK = 1

/** Ненастоящий код, которым Windows помечает служебные события клавиатуры. */
private const val FAKE_KEY = 0xFF

/** Флаги кнопок мыши `RI_MOUSE_*` (нажата, отпущена) → виртуальный код кнопки. */
private val MOUSE_BUTTONS = listOf(
    Triple(0x0001, 0x0002, 1), // левая
    Triple(0x0004, 0x0008, 2), // правая
    Triple(0x0010, 0x0020, 4), // средняя
    Triple(0x0040, 0x0080, 5), // боковая 1
    Triple(0x0100, 0x0200, 6), // боковая 2
)

/** Событие клавиатуры `RAWKEYBOARD` ([flags], [virtualKey]) или `null`, если это служебное событие. */
internal fun keyboardKey(flags: Int, virtualKey: Int): RawKey? =
    if (virtualKey == 0 || virtualKey == FAKE_KEY) null else RawKey(virtualKey, down = flags and KEY_BREAK == 0)

/** Кнопки из `usButtonFlags` события мыши `RAWMOUSE`: в одном событии их бывает несколько. */
internal fun mouseKeys(buttonFlags: Int): List<RawKey> = MOUSE_BUTTONS.flatMap { (down, up, code) ->
    listOfNotNull(RawKey(code, true).takeIf { buttonFlags and down != 0 }, RawKey(code, false).takeIf { buttonFlags and up != 0 })
}
