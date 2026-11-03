package com.layzen.bdotools.uikit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Формы элементов. В игре почти нет скруглений: углы прямые или 2dp. */
object AppShapes {
    val Sharp = RoundedCornerShape(0.dp)
    val Small = RoundedCornerShape(2.dp)
    val Pill = RoundedCornerShape(percent = 50)

    /** Всплывающие меню: заметно скруглены, чтобы отличаться от окон. */
    val Menu = RoundedCornerShape(6.dp)

    /** Подсветка пункта внутри всплывающего меню. */
    val MenuItem = RoundedCornerShape(4.dp)
}
