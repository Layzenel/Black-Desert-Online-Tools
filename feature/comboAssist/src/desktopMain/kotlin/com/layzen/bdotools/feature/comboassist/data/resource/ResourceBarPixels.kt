package com.layzen.bdotools.feature.comboassist.data.resource

import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.roundToInt

/** Красный пиксель полоски HP: она красная у всех классов. */
private fun isRed(p: Int): Boolean {
    val r = p shr 16 and 0xFF
    return r >= RED_LEVEL && r >= (p shr 8 and 0xFF) * RED_RATIO && r >= (p and 0xFF) * RED_RATIO
}

/** Не тёмный пиксель: заливка ресурса любого цвета светлее пустой части полоски (≈50). */
private fun isLit(p: Int): Boolean = maxOf(p shr 16 and 0xFF, p shr 8 and 0xFF, p and 0xFF) >= LIT_LEVEL

/**
 * Ищет полоску ресурса на снимке [image] (сторона единицы интерфейса — [unit] px). Цвет ресурса у классов разный
 * (белый, голубой, зелёный, красный…), поэтому ищется не цвет, а место: первая полоса рядов с длинной красной
 * заливкой — HP, а ресурс — первая полоса ниже неё (не тоньше двух рядов), где от левого края HP идёт длинная светлая
 * заливка до тёмной пустой части или рамки. Полоски бывают скошены (панель без «Разделения HP»), поэтому левый край
 * сверяется с нижним рядом HP, ближайшим к ресурсу. `null`, если HP или ресурса не видно (ресурс почти пуст — найдётся позже).
 */
internal fun locateResourceBar(image: BufferedImage, unit: Double): ResourceBarLock? {
    val minRun = (MIN_RUN_UNITS * unit).roundToInt()
    val red = (0 until image.height).map { y -> longestRun(image, y) { isRed(it) } }
    // полоса HP — самая длинная красная: выше неё бывают красные иконки бафов (у Страйкера иконка ~30 px сбивала поиск)
    val redBands = red.indices.filter { red[it].second >= minRun && (it == 0 || red[it - 1].second < minRun) }
        .map { start -> (start until red.size).takeWhile { red[it].second >= minRun } }
    val hpRows = redBands.maxByOrNull { band -> band.maxOf { red[it].second } } ?: return null
    val hpLeft = red[hpRows.last()].first
    val slack = (EDGE_UNITS * unit).roundToInt()
    val below = (hpRows.last() + 1 until minOf(image.height, hpRows.last() + (BELOW_UNITS * unit).roundToInt())).toList()
    val fills = below.map { y -> litRunNear(image, y, hpLeft, slack) }
    val minFill = (MIN_FILL_UNITS * unit).roundToInt()
    val lit = fills.map { it.second >= minFill }
    // полосы рядов с заливкой; одиночный ряд сразу под HP — нижняя кромка самой полоски HP
    val bands = lit.indices.filter { lit[it] && (it == 0 || !lit[it - 1]) }.map { start ->
        (start until lit.size).takeWhile { lit[it] }.take((MAX_ROWS_UNITS * unit).roundToInt())
    }
    val band = bands.firstOrNull { it.size >= MIN_ROWS } ?: return null
    return ResourceBarLock(below[band.first()]..below[band.last()], band.map { fills[it].first }.sorted()[band.size / 2])
}

/**
 * Длина заливки найденной полоски [lock] на снимке [image], px: в каждом ряду — светлые пиксели от левого края до
 * тёмной пустой части или рамки (блёстки и цифры на заливке светлые и её не обрывают); берётся самый длинный ряд.
 */
internal fun measureResourceBar(image: BufferedImage, lock: ResourceBarLock): Int = lock.rows.filter { it < image.height }.maxOfOrNull { y ->
    val start = lock.left + SKIP_EDGE
    if (start >= image.width || !isLit(image.getRGB(start, y))) return@maxOfOrNull 0
    var x = start
    while (x + 1 < image.width && isLit(image.getRGB(x + 1, y))) x++
    x + 1 - lock.left
} ?: 0

/** Самый длинный ряд подряд пикселей [match] в ряду [y]: начало и длина. */
private fun longestRun(image: BufferedImage, y: Int, match: (Int) -> Boolean): Pair<Int, Int> {
    var best = 0 to 0
    var start = -1
    for (x in 0..image.width) {
        val inRun = x < image.width && match(image.getRGB(x, y))
        if (inRun && start < 0) start = x
        if (!inRun && start >= 0) { if (x - start > best.second) best = start to x - start; start = -1 }
    }
    return best
}

/** Самая длинная светлая заливка в ряду [y], начинающаяся в пределах [slack] от [left]: начало и длина. */
private fun litRunNear(image: BufferedImage, y: Int, left: Int, slack: Int): Pair<Int, Int> {
    var best = left to 0
    for (start in (left - slack).coerceAtLeast(0)..(left + slack).coerceAtMost(image.width - 1)) {
        if (!isLit(image.getRGB(start, y))) continue
        var x = start
        while (x + 1 < image.width && isLit(image.getRGB(x + 1, y))) x++
        if (x + 1 - start > best.second) best = start to x + 1 - start
    }
    return best
}

private const val RED_LEVEL = 120
private const val RED_RATIO = 1.8
private const val LIT_LEVEL = 100

/** Полоска HP длиннее этого (в единицах интерфейса) — не случайная красная точка мира. */
private const val MIN_RUN_UNITS = 40.0

/** Насколько левый край ресурса может отстоять от левого края HP. */
private const val EDGE_UNITS = 8.0

/** Как далеко под HP искать ресурс. */
private const val BELOW_UNITS = 30.0

/** Заливка ресурса короче этого — не найдена (ресурс почти пуст или его не видно). */
private const val MIN_FILL_UNITS = 15.0

/** Полоса ресурса не тоньше этого, рядов. */
private const val MIN_ROWS = 2

/** Полоса ресурса не толще этого (в единицах интерфейса): ряды ниже — уже другие элементы. */
private const val MAX_ROWS_UNITS = 8.0

/** Край полоски бывает скруглён — заливка мерится с пары пикселей правее него. */
private const val SKIP_EDGE = 2
