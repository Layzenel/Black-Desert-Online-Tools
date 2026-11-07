package com.layzen.bdotools.feature.uieditor.domain

/**
 * Элемент интерфейса, который в игре не двигается и в файл не пишется («[не изменить]» в режиме правки игры):
 * редактор только показывает его, чтобы не ставить на него слоты. Прижат к нижнему углу экрана — правому,
 * если [atRight]. Ширина — доля ширины экрана [widthShare] (примерно), высота — [height] в UI-px.
 * Замерено по скриншотам 2560×1440 при масштабе 0.8.
 */
enum class FixedElement(val widthShare: Double, val height: Int, val atRight: Boolean) {
    /** Полоса подсказок «TIP» слева внизу. */
    Tips(widthShare = 0.16, height = 30, atRight = false),

    /** Панель кнопок меню справа внизу. */
    MenuBar(widthShare = 0.21, height = 48, atRight = true),
    ;

    /** Где элемент стоит на экране [screen], в UI-px от левого верхнего угла. */
    fun area(screen: ScreenSpace): ScreenArea {
        val width = screen.width * widthShare
        val left = if (atRight) screen.width - width else 0.0
        return ScreenArea(left, screen.height - height, left + width, screen.height)
    }
}
