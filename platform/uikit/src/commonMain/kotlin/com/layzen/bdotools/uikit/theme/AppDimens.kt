package com.layzen.bdotools.uikit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Отступы и размеры. В игре почти нет скруглений: углы прямые или 2dp. */
@Immutable
data class AppDimens(
    // Отступы
    val spaceXxs: Dp = 2.dp,
    val spaceXs: Dp = 4.dp,
    val spaceS: Dp = 8.dp,
    val spaceM: Dp = 12.dp,
    val spaceL: Dp = 16.dp,
    val spaceXl: Dp = 24.dp,

    // Линии
    val borderWidth: Dp = 1.dp,
    val selectedBorderWidth: Dp = 1.dp,
    val accentLineWidth: Dp = 2.dp,

    // Каркас страницы
    val topNavigationHeight: Dp = 48.dp,
    val contentMaxWidth: Dp = 760.dp,
    val sidebarWidth: Dp = 180.dp,

    // Окно
    val titleBarHeight: Dp = 40.dp,
    val footerHeight: Dp = 48.dp,

    // Кнопки и пункты
    val buttonHeight: Dp = 32.dp,
    val buttonMinWidth: Dp = 72.dp,
    val menuItemHeight: Dp = 44.dp,
    val menuIconBoxSize: Dp = 24.dp,
    val navItemHeight: Dp = 34.dp,
    val iconSize: Dp = 20.dp,
    val closeIconSize: Dp = 12.dp,

    // Меню по наведению
    val popupMenuWidth: Dp = 200.dp,
    val popupMenuItemHeight: Dp = 32.dp,
    val popupMenuItemInset: Dp = 3.dp,

    // Ожидание
    val spinnerSize: Dp = 36.dp,
    val spinnerStroke: Dp = 3.dp,
    val progressHeight: Dp = 4.dp,
    val busyOverlayWidth: Dp = 320.dp,

    // Поля ввода
    val inputHeight: Dp = 32.dp,
    val dropdownArrowWidth: Dp = 12.dp,
    val dropdownArrowHeight: Dp = 7.dp,

    // Переключатель
    val toggleWidth: Dp = 28.dp,
    val toggleHeight: Dp = 14.dp,
    val toggleThumbSize: Dp = 10.dp,

    // Вариант выбора
    val checkboxSize: Dp = 10.dp,
    val checkboxMarkSize: Dp = 6.dp,
    val checkBoxSize: Dp = 16.dp,
    val checkBoxTouchSize: Dp = 28.dp,

    // Слайдер
    val sliderTouchHeight: Dp = 20.dp,
    val sliderTrackHeight: Dp = 3.dp,
    val sliderThumbSize: Dp = 10.dp,
)

object AppShapes {
    val Sharp = RoundedCornerShape(0.dp)
    val Small = RoundedCornerShape(2.dp)
    val Pill = RoundedCornerShape(percent = 50)

    /** Всплывающие меню: заметно скруглены, чтобы отличаться от окон. */
    val Menu = RoundedCornerShape(6.dp)

    /** Подсветка пункта внутри всплывающего меню. */
    val MenuItem = RoundedCornerShape(4.dp)
}
