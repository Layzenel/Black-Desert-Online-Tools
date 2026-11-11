package com.layzen.bdotools.feature.uieditor.domain.game

/**
 * Элемент `<UIData>` из `gamevariable.xml` — положение одного окна или слота.
 *
 * [relativeX] и [relativeY] — левый верхний угол рамки в долях экрана: по ним игра ставит элемент.
 * [posX] и [posY] — смещение в UI-px от точки, которая зависит от привязки [pendingType]
 * (`RightBottom` — X считается справа налево, Y снизу вверх). Сама точка отсчёта у групп разная.
 */
data class UiData(
    val index: Int,
    val isShow: Boolean,
    val pendingType: String,
    val posX: Int,
    val posY: Int,
    val relativeX: Double,
    val relativeY: Double,
) {
    /** Привязан ли элемент к правому краю экрана. */
    val isAnchoredRight: Boolean get() = pendingType.startsWith("Right")
    /** Привязан ли элемент к нижнему краю экрана. */
    val isAnchoredBottom: Boolean get() = pendingType.endsWith("Bottom")
}
