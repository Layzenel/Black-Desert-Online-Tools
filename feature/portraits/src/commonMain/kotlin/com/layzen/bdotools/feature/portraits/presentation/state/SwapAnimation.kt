package com.layzen.bdotools.feature.portraits.presentation.state

/**
 * Анимация обмена двух портретов: в фазе [Phase.Out] старые картинки гаснут, сжимаясь,
 * в фазе [Phase.In] уже поменянные картинки проявляются с золотой рамкой.
 */
data class SwapAnimation(val first: String, val second: String, val phase: Phase) {
    enum class Phase { Out, In }

    fun involves(characterId: String) = characterId == first || characterId == second

    companion object {
        const val OUT_MS = 240
        const val IN_MS = 420
    }
}
