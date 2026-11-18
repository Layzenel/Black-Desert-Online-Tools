package com.layzen.bdotools.feature.comboassist.domain.tracker

/** Что трекер понял из очередного применённого умения. [chain] — что случилось со связкой, если она оборвалась. */
sealed interface TrackerEvent {
    val skillNo: Int?
    val chain: ChainRecovery?

    /** Шаг по комбо; [wrapped] — начался новый проход, [rescueEntry] — это вход в оборванную связку. */
    data class Step(override val skillNo: Int, val wrapped: Boolean = false, val rescueEntry: Boolean = false, override val chain: ChainRecovery? = null) : TrackerEvent

    /** Филлер на время ожидания: позиция не меняется. */
    data class Filler(override val skillNo: Int, override val chain: ChainRecovery? = null) : TrackerEvent

    /** Умение не из комбо; [count] — сколько таких подряд. */
    data class Off(override val skillNo: Int, val count: Int, override val chain: ChainRecovery? = null) : TrackerEvent

    /** Умение из комбо не по порядку, впереди на пути: позиция перешла на него, пропущено [skipped] шагов. */
    data class ErrorForward(override val skillNo: Int, val skipped: Int, override val chain: ChainRecovery? = null) : TrackerEvent

    /** Перескок по истории: совпало [matched] последних умений. */
    data class Jump(override val skillNo: Int, val matched: Int, override val chain: ChainRecovery? = null) : TrackerEvent

    /** Комбо начинается сначала. */
    data class Reset(val reason: ResetReason, override val skillNo: Int? = null, override val chain: ChainRecovery? = null) : TrackerEvent

    /** Связка оборвалась сама: продолжение не пришло вовремя. */
    data class ChainTimeout(override val chain: ChainRecovery) : TrackerEvent {
        override val skillNo: Int? get() = null
    }
}
