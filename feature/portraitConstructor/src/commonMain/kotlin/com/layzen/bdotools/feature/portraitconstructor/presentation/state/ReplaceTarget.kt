package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Что заменяем: все портреты сразу или один. */
sealed interface ReplaceTarget {
    data object All : ReplaceTarget
    data class One(val characterId: String) : ReplaceTarget
}
