package com.layzen.bdotools.core.account

/**
 * Аккаунт BDO на этом ПК — папка `UserCache\<id>`, в чьём `gamevariable.xml` есть порядок персонажей.
 * Портреты всех аккаунтов лежат вместе в `FaceTexture`, поэтому чужие отделяются по [characterOrder].
 *
 * @property characterOrder `CharacterNo` → место в окне выбора персонажа.
 * @property lastPlayed время изменения `gamevariable.xml` в миллисекундах: игра пишет файл, пока в неё играют.
 */
data class GameAccount(
    val id: String,
    val characterOrder: Map<String, Int>,
    val lastPlayed: Long,
) {
    /** ID персонажей в порядке окна выбора персонажа. */
    val characterIds: List<String> get() = characterOrder.entries.sortedBy { it.value }.map { it.key }
}
