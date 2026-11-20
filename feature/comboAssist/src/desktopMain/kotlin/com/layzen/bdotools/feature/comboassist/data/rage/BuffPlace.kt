package com.layzen.bdotools.feature.comboassist.data.rage

/**
 * Место, где на снимке нашлась иконка бафа.
 *
 * @property x левый край иконки на снимке, px
 * @property y верхний край иконки на снимке, px
 * @property score счёт совпадения с эталоном (меньше — похожее)
 */
internal data class BuffPlace(val x: Int, val y: Int, val score: Double)
