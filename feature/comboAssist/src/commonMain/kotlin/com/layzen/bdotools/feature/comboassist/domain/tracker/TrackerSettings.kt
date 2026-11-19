package com.layzen.bdotools.feature.comboassist.domain.tracker

/**
 * Числа, по которым трекер и подсказка принимают решения. Значения по умолчанию — догадки, их уточняют замеры в игре.
 *
 * @property stepMs сколько времени в среднем занимает один шаг комбо
 * @property chainWindowMs сколько можно медлить до продолжения связки, пока оно ещё выходит
 * @property idleMs сколько можно не жать умения комбо и филлеры сверх ожидания перезарядки — потом комбо начинается сначала
 * @property offLimit сколько умений не из комбо подряд — и комбо начинается сначала
 * @property skipWeight во сколько шагов по времени обходится пропуск одного шага
 * @property fillerMinMs меньше этого ожидания филлер не предлагается
 * @property animationMs длительность анимации филлера, пока нет замеров из истории
 * @property predictLimit сколько шагов вперёд считать подсказку
 */
data class TrackerSettings(
    val stepMs: Long = 800,
    val chainWindowMs: Long = 1500,
    val idleMs: Long = 5000,
    val offLimit: Int = 3,
    val skipWeight: Double = 1.5,
    val fillerMinMs: Long = 600,
    val animationMs: Long = 700,
    val predictLimit: Int = 16,
)
