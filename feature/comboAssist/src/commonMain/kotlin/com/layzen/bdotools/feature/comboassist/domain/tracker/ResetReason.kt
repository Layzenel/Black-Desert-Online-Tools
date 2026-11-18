package com.layzen.bdotools.feature.comboassist.domain.tracker

/** Почему комбо началось сначала. */
enum class ResetReason {
    /** Подряд несколько умений не из комбо. */
    OffCombo,

    /** Долго не было умений. */
    Idle,

    /** Игрок сам начал заново. */
    Manual,
}
