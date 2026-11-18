package com.layzen.bdotools.feature.comboassist.data.history

private const val WHITE_MIN = 185
private const val RED_MIN = 190
private const val RED_OVER_GREEN = 60
private const val GREEN_OVER_BLUE = 20
private const val RED_BLUE_MAX = 150

/**
 * Пиксель текста строки истории умений: белый (обычные умения) или красно-оранжевый, около `#F09664`
 * («Мощь: …» и, возможно, другие категории; замер 2026-11-14). Зелень и камни фона под строкой — не текст.
 */
internal fun isHistoryTextPixel(rgb: Int): Boolean {
    val r = rgb shr 16 and 0xFF
    val g = rgb shr 8 and 0xFF
    val b = rgb and 0xFF
    val white = minOf(r, g, b) > WHITE_MIN
    val red = r > RED_MIN && r - g > RED_OVER_GREEN && g - b > GREEN_OVER_BLUE && b < RED_BLUE_MAX
    return white || red
}
