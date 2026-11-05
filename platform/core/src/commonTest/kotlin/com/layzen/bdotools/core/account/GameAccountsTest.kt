package com.layzen.bdotools.core.account

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GameAccountsTest {

    private val files = object : AccountFiles {
        var list: List<AccountFile> = emptyList()
        override suspend fun accountFiles() = list
        override suspend fun readPortrait(characterId: String): ByteArray? = null
    }

    private val choice = object : AccountChoice {
        var saved: String? = null
        override fun load() = saved
        override fun save(id: String) {
            saved = id
        }
    }

    private fun file(id: String, modified: Long, vararg characters: String) = AccountFile(
        id,
        modified,
        characters.withIndex().joinToString("\n") { (order, no) -> """<CharacterOrderList CharacterNo="$no" Order="$order"/>""" },
    )

    @Test
    fun skipsFilesWithoutCharactersAndSortsByLastPlayed() = runTest {
        files.list = listOf(file("old", 1, "11"), file("empty", 5), file("new", 3, "22", "21"))
        val accounts = GameAccounts(files, choice).apply { refresh() }

        assertEquals(listOf("new", "old"), accounts.state.value.accounts.map { it.id })
        assertEquals(listOf("22", "21"), accounts.state.value.accounts.first().characterIds)
    }

    @Test
    fun singleAccountIsSelectedWithoutChoice() = runTest {
        files.list = listOf(file("only", 1, "11"))
        val state = GameAccounts(files, choice).apply { refresh() }.state.value

        assertEquals("only", state.selected?.id)
        assertFalse(state.needsChoice)
    }

    @Test
    fun severalAccountsNeedChoiceUntilSelectedAndChoiceIsRemembered() = runTest {
        files.list = listOf(file("a", 1, "11"), file("b", 2, "21"))
        val accounts = GameAccounts(files, choice).apply { refresh() }
        assertTrue(accounts.state.value.needsChoice)
        assertNull(accounts.state.value.selected)

        accounts.select("a")

        assertEquals("a", accounts.state.value.selected?.id)
        assertEquals("a", GameAccounts(files, choice).apply { refresh() }.state.value.selected?.id)
    }

    @Test
    fun rememberedAccountThatIsGoneNeedsNewChoice() = runTest {
        choice.saved = "deleted"
        files.list = listOf(file("a", 1, "11"), file("b", 2, "21"))

        assertTrue(GameAccounts(files, choice).apply { refresh() }.state.value.needsChoice)
    }
}
