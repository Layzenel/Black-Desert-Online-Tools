package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.roundToInt

/**
 * Позиция элемента по записи: `RelativePos` — центр иконки в долях экрана.
 * Это видно на скриншоте игры: центры иконок совпадают с `RelativePos × экран` до пикселя.
 */
fun UiData.position(screen: ScreenSpace) = SlotPosition(
    x = (relativeX * screen.width - screen.width / 2).roundToInt(),
    y = (relativeY * screen.height - screen.height / 2).roundToInt(),
)

/**
 * Запись после переноса элемента в [position]. `RelativePos` считается напрямую из позиции,
 * а `PosX`/`PosY` сдвигаются на ту же величину: их точку отсчёта мы не знаем, но сдвиг от неё не зависит.
 * Направление сдвига задаёт привязка [UiData.pendingType]. Привязка остаётся прежней.
 */
fun UiData.movedTo(position: SlotPosition, screen: ScreenSpace): UiData {
    val relativeX = (screen.width / 2 + position.x) / screen.width
    val relativeY = (screen.height / 2 + position.y) / screen.height
    val dx = (relativeX - this.relativeX) * screen.width
    val dy = (relativeY - this.relativeY) * screen.height
    return copy(
        posX = (posX + if (isAnchoredRight) -dx else dx).roundToInt(),
        posY = (posY + if (isAnchoredBottom) -dy else dy).roundToInt(),
        relativeX = relativeX,
        relativeY = relativeY,
    )
}
