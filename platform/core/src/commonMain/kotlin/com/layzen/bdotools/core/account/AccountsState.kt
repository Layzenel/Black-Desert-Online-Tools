package com.layzen.bdotools.core.account

/**
 * Аккаунты на этом ПК и выбранный из них.
 *
 * @property accounts от последнего сыгранного к давним; пусто, пока папку не прочитали.
 * @property selectedId сохранённый выбор; может указывать на аккаунт, которого уже нет.
 */
data class AccountsState(
    val accounts: List<GameAccount> = emptyList(),
    val selectedId: String? = null,
) {
    /** Аккаунт, с которым работают тулзы. Если аккаунт один, выбирать его не нужно. */
    val selected: GameAccount? get() = accounts.firstOrNull { it.id == selectedId } ?: accounts.singleOrNull()

    /** Аккаунтов несколько, а какой из них нужен, ещё не выбрано. */
    val needsChoice: Boolean get() = accounts.size > 1 && selected == null
}
