package com.layzen.bdotools.feature.screenshoteditor.domain.adjust

import kotlin.math.pow

// Яркость ±100 сдвигает каналы на четверть диапазона, температура и оттенок — множитель канала ±20 %
private const val BRIGHTNESS_SHIFT = 64f
private const val WARMTH = 0.2f
private const val MID_GRAY = 128f
private const val LUMA_R = 0.2126f
private const val LUMA_G = 0.7152f
private const val LUMA_B = 0.0722f
private const val ROW = 5

/**
 * Матрица цвета 4×5 по строкам (R, G, B, A; четыре множителя и сдвиг в единицах 0–255) — формат `ColorMatrix`
 * Compose. Шаги по порядку: экспозиция, температура и оттенок, насыщенность, контраст, яркость.
 */
fun Adjustments.colorMatrix(): FloatArray {
    val exposureScale = 2f.pow(exposure / 100f)
    val warm = temperature / 100f * WARMTH
    val magenta = tint / 100f * WARMTH
    val steps = listOf(
        scale(exposureScale, exposureScale, exposureScale),
        scale(1 + warm, 1 - magenta, 1 - warm),
        saturationMatrix(1 + saturation / 100f),
        contrastMatrix(1 + contrast / 100f),
        offset(brightness / 100f * BRIGHTNESS_SHIFT),
    )
    return steps.reduce { applied, next -> next.after(applied) }
}

/** Цвет [rgba] (каналы 0–255) после матрицы [this] — для проверки и тестов. */
fun FloatArray.apply(rgba: FloatArray): FloatArray = FloatArray(4) { row ->
    (0 until 4).sumOf { (this[row * ROW + it] * rgba[it]).toDouble() }.toFloat() + this[row * ROW + 4]
}

private fun identity() = scale(1f, 1f, 1f)

private fun scale(r: Float, g: Float, b: Float) = FloatArray(20).also {
    it[0] = r
    it[6] = g
    it[12] = b
    it[18] = 1f
}

private fun offset(shift: Float) = identity().also {
    it[4] = shift
    it[9] = shift
    it[14] = shift
}

private fun contrastMatrix(k: Float) = scale(k, k, k).also {
    val shift = MID_GRAY * (1 - k)
    it[4] = shift
    it[9] = shift
    it[14] = shift
}

private fun saturationMatrix(s: Float): FloatArray {
    val luma = floatArrayOf(LUMA_R, LUMA_G, LUMA_B)
    return FloatArray(20).also { m ->
        for (row in 0 until 3) for (col in 0 until 3) {
            m[row * ROW + col] = (1 - s) * luma[col] + if (row == col) s else 0f
        }
        m[18] = 1f
    }
}

/** Матрица «сначала [first], потом эта». */
private fun FloatArray.after(first: FloatArray): FloatArray = FloatArray(20).also { out ->
    for (row in 0 until 4) {
        for (col in 0 until 4) out[row * ROW + col] = (0 until 4).sumOf { (this[row * ROW + it] * first[it * ROW + col]).toDouble() }.toFloat()
        out[row * ROW + 4] = (0 until 4).sumOf { (this[row * ROW + it] * first[it * ROW + 4]).toDouble() }.toFloat() + this[row * ROW + 4]
    }
}
