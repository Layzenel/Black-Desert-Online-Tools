package com.layzen.bdotools.feature.comboassist.presentation.state

import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.tracker.PlannedStep
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerEvent

/**
 * Снимок трекера для оверлея: один и тот же в тренировке и в окне поверх игры.
 *
 * @property nodeAt номер шага графа по ячейке комбо
 * @property plan путь подсказки до конца прохода; первый шаг — что жать сейчас
 * @property current шаги, на которых игрок сейчас
 * @property done пройденные в этом проходе
 * @property burned сгоревшие в этом проходе
 * @property pass номер прохода; 0 — комбо ещё не начато
 * @property cooldownLeft сколько осталось до отката, мс, по номеру навыка (только умения на перезарядке)
 * @property filler умение-филлер на время ожидания или `null`
 * @property optional опциональные умения, которые можно вставить сейчас: трекер показывает их отдельно от пути
 * @property previousSkill последнее применённое умение: по нему выбирается строка команды следующего
 * @property event последнее событие трекера, пока оно свежее
 * @property nowMs момент снимка
 */
data class OverlayState(
    val nodeAt: Map<CellAt, Int> = emptyMap(),
    val plan: List<PlannedStep> = emptyList(),
    val current: Set<Int> = emptySet(),
    val done: Set<Int> = emptySet(),
    val burned: Set<Int> = emptySet(),
    val pass: Int = 0,
    val cooldownLeft: Map<Int, Long> = emptyMap(),
    val filler: Int? = null,
    val optional: List<Int> = emptyList(),
    val previousSkill: Int? = null,
    val event: TrackerEvent? = null,
    val nowMs: Long = 0,
) {
    /** Что жать сейчас. */
    val next: PlannedStep? get() = plan.firstOrNull()

    /** Сколько ждать следующий шаг, мс. */
    val waitMs: Long get() = next?.let { (it.atMs - nowMs).coerceAtLeast(0) } ?: 0
}
