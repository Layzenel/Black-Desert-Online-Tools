package com.layzen.bdotools.feature.slots.domain

import kotlin.math.roundToInt

/** Позиция слота группы [group], который игра ставит по этой записи. */
fun UiData.position(group: SlotGroup, screen: ScreenSpace) = SlotPosition(
    x = (relativeX * screen.width + group.width / 2.0 - screen.width / 2).roundToInt(),
    y = (relativeY * screen.height + group.height / 2.0 - screen.height / 2).roundToInt(),
)

/**
 * Запись после переноса слота в [position]. `RelativePos` считается напрямую из позиции,
 * а `PosX`/`PosY` сдвигаются на ту же величину: их точку отсчёта мы не знаем, но сдвиг от неё не зависит.
 * Направление сдвига задаёт привязка [UiData.pendingType]. Привязка остаётся прежней.
 */
fun UiData.movedTo(position: SlotPosition, group: SlotGroup, screen: ScreenSpace): UiData {
    val left = screen.frameLeft(group, position)
    val top = screen.frameTop(group, position)
    val dx = left - relativeX * screen.width
    val dy = top - relativeY * screen.height
    return copy(
        posX = (posX + if (isAnchoredRight) -dx else dx).roundToInt(),
        posY = (posY + if (isAnchoredBottom) -dy else dy).roundToInt(),
        relativeX = left / screen.width,
        relativeY = top / screen.height,
    )
}
