package com.layzen.bdotools.feature.comboassist.data.history

/**
 * Новая строка истории, которую [HistoryLines] не заметил: иконка нижней строки сменилась на иконку другого умения.
 *
 * [HistoryLines] видит новую строку по тексту — нижняя строка пустеет при сдвиге. Текст бывает не того цвета
 * («Абсолют: …») или умения идут так быстро, что пустую нижнюю строку не застать, и строку пропускали
 * (2026-11-26, Страйкер: «Абсолют: Удар лодыжкой» между «Мощь: Стальное плечо IV» и «Абсолют: Железный кулак» —
 * в истории игры есть, в журнале ни разу). Иконка у нижней строки есть всегда. Смена засчитывается, когда другое
 * умение узнано на [FRAMES] кадрах подряд: одиночный кадр бывает переходом между иконками. Повтор того же умения так
 * не заметить — его ловит [HistoryLines].
 */
internal class HistoryIconChange {
    private var last: Int? = null
    private var candidate: Int? = null
    private var seen = 0

    /** Строка с умением [skillNo] узнана обычным путём: от него и считаются смены. */
    fun recognized(skillNo: Int) {
        last = skillNo
        candidate = null
    }

    /** Кадр, где иконка нижней строки узнана как [skillNo] (`null` — не узнана) → умение новой строки или `null`. */
    fun onFrame(skillNo: Int?): Int? {
        if (skillNo == null || last == null || skillNo == last) {
            candidate = null
            return null
        }
        seen = if (skillNo == candidate) seen + 1 else 1
        candidate = skillNo
        if (seen < FRAMES) return null
        recognized(skillNo)
        return skillNo
    }

    private companion object {
        const val FRAMES = 2
    }
}
