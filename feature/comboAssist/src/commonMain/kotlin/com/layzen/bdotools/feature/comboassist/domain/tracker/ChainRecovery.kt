package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.graph.ComboNode

/** Что делать, когда связка оборвалась перед шагом [brokenAt]; выбирается самое дешёвое по времени. */
sealed interface ChainRecovery {
    val brokenAt: ComboNode

    /** Войти в продолжение через другое умение [entry], которое готово. */
    data class Rescue(override val brokenAt: ComboNode, val entry: Int) : ChainRecovery

    /** Пройти связку заново с запуска [launcher], когда он откатится. */
    data class Relaunch(override val brokenAt: ComboNode, val launcher: ComboNode) : ChainRecovery

    /** Пропустить остаток связки: [lost] шагов. */
    data class Skip(override val brokenAt: ComboNode, val lost: Int) : ChainRecovery
}
