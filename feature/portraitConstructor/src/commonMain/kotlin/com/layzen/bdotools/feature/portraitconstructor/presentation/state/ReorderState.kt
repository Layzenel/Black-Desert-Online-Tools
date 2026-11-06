package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/**
 * Режим изменения порядка на сетке портретов: [first] — уже выбранный портрет.
 * Как только выбран второй, два портрета меняются местами и выбор начинается заново.
 */
data class ReorderState(val first: String? = null) {

    /** Пара для обмена, если [characterId] — второй выбранный портрет. */
    fun pairWith(characterId: String): Pair<String, String>? =
        first?.takeIf { it != characterId }?.let { it to characterId }

    /** Выбирает первый портрет; повторный клик по нему снимает выбор. */
    fun toggled(characterId: String): ReorderState = copy(first = if (first == characterId) null else characterId)

    companion object {
        /** Сколько портретов меняются местами. */
        const val SWAP_SIZE = 2
    }
}
