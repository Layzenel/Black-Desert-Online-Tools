package com.layzen.bdotools.feature.uieditor.domain.layout

import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace

/**
 * Элемент интерфейса, который в игре не двигается и в файл не пишется («[не изменить]» в режиме правки игры):
 * редактор только показывает его, чтобы не ставить на него слоты. Прижат к углу экрана: к правому, если [atRight],
 * к верхнему, если [atTop]. Ширина — доля ширины экрана [widthShare] (примерно) плюс [width] в UI-px,
 * высота — [height] в UI-px. Замерено по скриншотам 2560×1440 при масштабе 0.8.
 */
enum class FixedElement(
    val widthShare: Double = 0.0,
    val width: Int = 0,
    val height: Int,
    val atRight: Boolean = false,
    val atTop: Boolean = false,
) {
    /** Полоса подсказок «TIP» слева внизу. */
    Tips(widthShare = 0.16, height = 30),

    /** Панель кнопок меню справа внизу. */
    MenuBar(widthShare = 0.21, height = 48, atRight = true),

    /**
     * Здоровье, PVP и гнев Чёрного духа одной панелью слева вверху: её показывает игра при выключенном
     * «Разделении HP» (см. [SlotLayout.splitHp]), в файле это `UIData Index="138"` без позиции.
     */
    CombinedHp(width = 480, height = 133, atTop = true),
    ;

    /** Есть ли элемент на экране при «Разделении HP» [splitHp]; `null` — как включённое. */
    fun isShownWith(splitHp: Boolean?): Boolean = this != CombinedHp || splitHp == false

    /** Где элемент стоит на экране [screen], в UI-px от левого верхнего угла. */
    fun area(screen: ScreenSpace): ScreenArea {
        val elementWidth = screen.width * widthShare + width
        val left = if (atRight) screen.width - elementWidth else 0.0
        val top = if (atTop) 0.0 else screen.height - height
        return ScreenArea(left, top, left + elementWidth, top + height)
    }
}
