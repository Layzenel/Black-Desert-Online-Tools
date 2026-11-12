package com.layzen.bdotools.uikit.theme

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
    val introImageMaxHeight: Dp = 480.dp,

    // Окно
    val titleBarHeight: Dp = 40.dp,
    val footerHeight: Dp = 48.dp,

    // Кнопки и пункты
    val buttonHeight: Dp = 32.dp,
    val buttonMinWidth: Dp = 72.dp,
    val menuItemHeight: Dp = 44.dp,
    val menuIconBoxSize: Dp = 24.dp,
    val navItemHeight: Dp = 34.dp,
    val menuActionHeight: Dp = 32.dp,
    val menuActionIconSize: Dp = 16.dp,
    val iconSize: Dp = 20.dp,
    val infoTipMaxWidth: Dp = 260.dp,

    // Списки и прокрутка
    val optionsMenuMaxHeight: Dp = 320.dp,
    val contextMenuWidth: Dp = 260.dp,
    // Подсказка при наведении: длинный текст переносится, а не тянется в одну строку
    val tooltipMaxWidth: Dp = 260.dp,
    val groupedMenuOptionsWidth: Dp = 220.dp,
    val menuShadow: Dp = 3.dp,
    val tooltipFrameGap: Dp = 2.dp,
    val swatchSize: Dp = 22.dp,
    val scrollbarWidth: Dp = 4.dp,
    val scrollbarHoverWidth: Dp = 8.dp,
    val scrollbarInset: Dp = 2.dp,
    val closeIconSize: Dp = 12.dp,

    // Меню действий по наведению
    val actionMenuItemHeight: Dp = 32.dp,
    val actionMenuItemInset: Dp = 3.dp,
    val hoverBlurRadius: Dp = 6.dp,

    // Ожидание
    val loaderSize: Dp = 44.dp,
    val loaderSmallSize: Dp = 26.dp,
    val loaderOutline: Dp = 1.dp,
    val progressHeight: Dp = 4.dp,
    val busyOverlayWidth: Dp = 320.dp,

    // Всплывающие сообщения
    val toastMaxWidth: Dp = 560.dp,
    val toastBottomOffset: Dp = 32.dp,

    // Поля ввода
    val inputHeight: Dp = 32.dp,
    val dropdownArrowWidth: Dp = 12.dp,
    val dropdownArrowHeight: Dp = 7.dp,
    // Место под элемент справа от варианта списка (корзина): не уже, чем отводит пункт меню Material3
    val menuTrailingWidth: Dp = 24.dp,

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
