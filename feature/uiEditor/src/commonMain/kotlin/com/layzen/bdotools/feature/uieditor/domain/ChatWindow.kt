package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.roundToInt

/** Меньше этого окно чата в редакторе не сжимается, UI-px. */
internal const val CHAT_MIN_WIDTH = 200
internal const val CHAT_MIN_HEIGHT = 100

/**
 * Какую долю ширины экрана может занять чат: замерено по скриншоту растянутого до упора окна 2560×1440.
 * shortcut: замер один, при другом разрешении или масштабе предел может оказаться числом в UI-px — тогда заменить.
 */
private const val CHAT_MAX_WIDTH_SHARE = 2.0 / 3

/**
 * Окно чата — запись `UIData` с `Index="32"`; окна различает `WindowIndex`, он же ключ в [SlotLayout.chats].
 * [center] — центр окна от центра экрана, как у слотов (`RelativePosX/Y`), [width] и [height] — `SizeX/SizeY` в UI-px.
 * [visible] — `IsShow`, глазик в режиме правки
 * интерфейса; на экране окно ещё зависит от главного чата, см. [SlotLayout.isChatShown]. [combined] —
 * `IsCombinedToMainPanel`: чат — вкладка главного окна, а не отдельное окно; у главного всегда `false`.
 */
data class ChatWindow(
    val center: SlotPosition,
    val width: Int,
    val height: Int,
    val visible: Boolean = true,
    val combined: Boolean = false,
) {

    /** Левый край окна на экране [screen] в UI-px от левого края экрана. */
    fun left(screen: ScreenSpace): Double = screen.width / 2 + center.x - width / 2.0

    fun top(screen: ScreenSpace): Double = screen.height / 2 + center.y - height / 2.0

    /** Рамка окна в UI-px от центра экрана — как у слотов, для прилипания. */
    val bounds: SlotBounds
        get() = SlotBounds(center.x - width / 2.0, center.y - height / 2.0, center.x + width / 2.0, center.y + height / 2.0)

    fun movedBy(dx: Int, dy: Int) = copy(center = center.movedBy(dx, dy))

    /** Помещается ли окно на экран [screen] целиком. */
    fun fits(screen: ScreenSpace): Boolean =
        left(screen) >= 0 && top(screen) >= 0 && left(screen) + width <= screen.width && top(screen) + height <= screen.height

    /** Окно размером [newWidth]×[newHeight] (в пределах, как при растягивании), левый нижний угол на месте. */
    fun withSize(newWidth: Int, newHeight: Int, screen: ScreenSpace) = resizedFromTopRight(newWidth - width, height - newHeight, screen)

    /**
     * Окно, растянутое за правый верхний угол на [dx], [dy], как в игре: левый нижний угол стоит на месте,
     * размер — в пределах от минимума до [maxWidth] и высоты экрана.
     */
    fun resizedFromTopRight(dx: Int, dy: Int, screen: ScreenSpace): ChatWindow {
        val newWidth = (width + dx).coerceIn(CHAT_MIN_WIDTH, maxWidth(screen))
        val newHeight = (height - dy).coerceIn(CHAT_MIN_HEIGHT, screen.height.toInt())
        val left = center.x - width / 2.0
        val bottom = center.y + height / 2.0
        return copy(
            center = SlotPosition((left + newWidth / 2.0).roundToInt(), (bottom - newHeight / 2.0).roundToInt()),
            width = newWidth,
            height = newHeight,
        )
    }

    /** Чат, вынутый из главного окна [main] в отдельное: видимое окно своего размера прямо над [main], по его левому краю. */
    fun detachedAbove(main: ChatWindow): ChatWindow {
        val left = main.center.x - main.width / 2.0
        val mainTop = main.center.y - main.height / 2.0
        return copy(
            center = SlotPosition((left + width / 2.0).roundToInt(), (mainTop - height / 2.0).roundToInt()),
            visible = true,
            combined = false,
        )
    }

    /** То же окно, но не шире, чем игра позволит на экране [screen]. */
    fun fittedTo(screen: ScreenSpace): ChatWindow = copy(width = width.coerceAtMost(maxWidth(screen)))

    companion object {
        /** `WindowIndex` главного чата: к нему прикреплены вкладки, остальные окна от него зависят. */
        const val MAIN = 0

        /** Самое широкое окно чата на экране [screen]. */
        fun maxWidth(screen: ScreenSpace): Int = (screen.width * CHAT_MAX_WIDTH_SHARE).toInt()
    }
}
