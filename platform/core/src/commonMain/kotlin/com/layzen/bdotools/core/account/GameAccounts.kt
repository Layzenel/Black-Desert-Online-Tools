package com.layzen.bdotools.core.account

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Аккаунты BDO на этом ПК, общие для всех тулз и верхней навигации. Фича зовёт [refresh], когда получила
 * доступ к папке, и берёт [AccountsState.selected]; смену аккаунта видно по [state].
 */
class GameAccounts(private val files: AccountFiles, private val choice: AccountChoice) {

    private val mutableState = MutableStateFlow(AccountsState(selectedId = choice.load()))

    /** Аккаунты на этом ПК и выбранный из них; меняется через [refresh] и [select]. */
    val state: StateFlow<AccountsState> = mutableState.asStateFlow()

    /**
     * Перечитывает аккаунты. Аккаунтом считается только `gamevariable.xml` с персонажами: в корне `UserCache`
     * и у аккаунтов, где персонажа ни разу не переключали, список пустой. Если прочитать не вышло, список
     * пустеет и тулзы работают со всей папкой, как до выбора аккаунтов.
     */
    suspend fun refresh() {
        val accounts = try {
            files.accountFiles().mapNotNull(::toAccount).sortedByDescending { it.lastPlayed }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
        mutableState.update { it.copy(accounts = accounts) }
    }

    /** Делает аккаунт [id] выбранным и запоминает выбор между запусками. */
    fun select(id: String) {
        choice.save(id)
        mutableState.update { it.copy(selectedId = id) }
    }

    /** Портрет персонажа из `FaceTexture` для окна выбора аккаунта; `null`, если файла нет. */
    suspend fun readPortrait(characterId: String): ByteArray? = files.readPortrait(characterId)

    private fun toAccount(file: AccountFile): GameAccount? {
        val order = parseCharacterOrder(file.gameVariable)
        return if (order.isEmpty()) null else GameAccount(file.id, order, file.modified)
    }
}
