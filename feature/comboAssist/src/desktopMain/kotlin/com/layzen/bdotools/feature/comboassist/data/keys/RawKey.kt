package com.layzen.bdotools.feature.comboassist.data.keys

/**
 * Клавиша или кнопка мыши нажата или отпущена.
 *
 * @property keyCode виртуальный код клавиши Windows (кнопки мыши: 1 — левая, 2 — правая, 4 — средняя, 5 и 6 — боковые)
 * @property down нажата (`false` — отпущена)
 */
internal data class RawKey(val keyCode: Int, val down: Boolean)
