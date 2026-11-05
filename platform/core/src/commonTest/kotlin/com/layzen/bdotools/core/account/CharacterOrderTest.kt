package com.layzen.bdotools.core.account

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterOrderTest {

    private val gameVariable = """
        <ExchangeCoinBookmark Version="1">
        	<BookmarkCoin ItemKey="65396"/>
        </ExchangeCoinBookmark>
        <CharacterOrderList Version="1">
        	<CharacterOrderList CharacterNo="40000000003247543" Order="0"/>
        	<CharacterOrderList CharacterNo="40000000003249407" Order="2"/>
        	<CharacterOrderList CharacterNo="40000000008825214" Order="1"/>
        </CharacterOrderList>
        <EmployeeP Version="1"/>
    """.trimIndent()

    @Test
    fun parsesOrderFromFileWithoutRootTag() {
        assertEquals(
            mapOf(
                "40000000003247543" to 0,
                "40000000003249407" to 2,
                "40000000008825214" to 1,
            ),
            parseCharacterOrder(gameVariable),
        )
    }

    @Test
    fun emptyOrderListGivesEmptyMap() {
        assertTrue(parseCharacterOrder("""<CharacterOrderList Version="1"/>""").isEmpty())
    }
}
