package com.layzen.bdotools.feature.portraits.presentation.state

/**
 * Анимация обмена двух портретов, как в игре: в фазе [Phase.Out] плитки заливает золотом с искрами,
 * на её конце картинки меняются, в фазе [Phase.In] вспышка гаснет уже над новыми картинками.
 */
data class SwapAnimation(val first: String, val second: String, val phase: Phase) {
    enum class Phase { Out, In }

    fun involves(characterId: String) = characterId == first || characterId == second

    companion object {
        const val OUT_MS = 410
        const val IN_MS = 340
        const val TOTAL_MS = OUT_MS + IN_MS
    }
}
