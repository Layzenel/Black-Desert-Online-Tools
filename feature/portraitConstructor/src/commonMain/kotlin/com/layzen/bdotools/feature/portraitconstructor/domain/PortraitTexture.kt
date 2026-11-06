package com.layzen.bdotools.feature.portraitconstructor.domain

/**
 * Размеры портрета в игре.
 *
 * [WIDTH] × [HEIGHT] — размер файлов в `FaceTexture`. [GAP] — расстояние между ячейками в окне выбора
 * персонажа, пересчитанное в пиксели текстуры: в окне игры ячейка 100×128, а отступ 4 px
 * (4 / 100 × 624 ≈ 25 по горизонтали, 4 / 128 × 804 ≈ 25 по вертикали).
 */
object PortraitTexture {
    const val WIDTH = 624
    const val HEIGHT = 804
    const val GAP = 25
    const val MAX_COLUMNS = 7
}
