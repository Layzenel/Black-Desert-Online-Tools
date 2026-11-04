package com.layzen.bdotools.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateInfoTest {

    @Test
    fun versionsCompareByNumbers() {
        assertTrue(AppVersion.parse("1.0.10")!! > AppVersion.parse("1.0.9")!!)
        assertTrue(AppVersion.parse("1.1")!! > AppVersion.parse("1.0.99")!!)
        assertEquals(0, AppVersion.parse("1.0")!!.compareTo(AppVersion.parse("1.0.0")!!))
        assertNull(AppVersion.parse("1.0.x"))
        assertNull(AppVersion.parse(""))
    }

    @Test
    fun parsesUpdateFile() {
        val info = UpdateInfo.parse(
            """
            version=1.0.57
            msi=https://example.com/BDO-Tools.msi
            zip=https://example.com/BDO-Tools-portable.zip
            note=Reorder portraits
            note=
            note=Toasts = notices
            unknown=1
            """.trimIndent(),
        )!!

        assertEquals(AppVersion(listOf(1, 0, 57)), info.version)
        assertEquals("https://example.com/BDO-Tools-portable.zip", info.zipUrl)
        assertEquals(listOf("Reorder portraits", "Toasts = notices"), info.notes)
    }

    @Test
    fun updateFileWithoutVersionOrArchiveIsIgnored() {
        assertNull(UpdateInfo.parse("zip=https://example.com/a.zip"))
        assertNull(UpdateInfo.parse("version=1.0.3\nmsi=https://example.com/a.msi"))
    }
}
