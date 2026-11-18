package com.layzen.bdotools.core.skill.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeyBindingsTest {

    @Test
    fun readsActionKeysAndSkipsUnassigned() {
        val file = """
            <ShortcutKey Version="15">
            	<ShortcutKey_Action_Key InputType="Dash" Alt="false" IgnoreFuncKey="false" keyCode="16"/>
            	<ShortcutKey_Action_Key InputType="CrouchOrSkill" Alt="false" IgnoreFuncKey="false" keyCode="81"/>
            	<ShortcutKey_Action_Key InputType="QuickSlot6" Alt="false" IgnoreFuncKey="false" keyCode="0"/>
            	<ShortcutKey_Action_Pad InputType="Dash" keyCode="99"/>
            </ShortcutKey>
        """.trimIndent()

        val keys = parseKeyBindings(file)

        assertEquals(mapOf("Dash" to 16, "CrouchOrSkill" to 81), keys.keyCodes)
        assertNull(keys["QuickSlot6"])
    }
}
