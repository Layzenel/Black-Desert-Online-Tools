package com.layzen.bdotools.uikit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Формы элементов. В игре почти нет скруглений: углы прямые или 2–3dp. */
object AppShapes {
    val Sharp = RoundedCornerShape(0.dp)
    val Small = RoundedCornerShape(2.dp)
    val Pill = RoundedCornerShape(percent = 50)

    /** Кнопки и варианты ([com.layzen.bdotools.uikit.components.OptionButton]): скругление, снятое со скриншотов игры. */
    val Control = RoundedCornerShape(3.dp)

    /** Внутренняя кромка [Control] — на толщину рамки меньше. */
    val ControlInner = RoundedCornerShape(2.dp)

    /** Подсветка пункта в меню действий. */
    val MenuItem = RoundedCornerShape(4.dp)
}
