package com.layzen.bdotools.feature.portraitconstructor.domain.backup

import kotlin.test.Test
import kotlin.test.assertEquals

class BackupsTest {

    private val listing = listOf(
        "2026-11-03_12-00-05\t1.bmp,2.bmp",
        "2026-11-04_09-30-00\t2.bmp",
        "2026-11-03_12-00-05_2\t1.bmp",
        "empty\t",
        "2026-11-01_08-00-00\tnotes.txt",
    ).joinToString("\n")

    @Test
    fun newestFirstAndEmptySkipped() {
        assertEquals(
            listOf("2026-11-04_09-30-00", "2026-11-03_12-00-05_2", "2026-11-03_12-00-05"),
            parseBackupList(listing).map { it.name },
        )
    }

    @Test
    fun idsWithoutExtension() {
        assertEquals(setOf("1", "2"), parseBackupList(listing).last().characterIds)
    }

    @Test
    fun readableLabel() {
        assertEquals("2026-11-03 12:00:05", BackupInfo("2026-11-03_12-00-05", emptySet()).label)
        assertEquals("2026-11-03 12:00:05 (2)", BackupInfo("2026-11-03_12-00-05_2", emptySet()).label)
        assertEquals("ручная копия", BackupInfo("ручная копия", emptySet()).label)
    }

    @Test
    fun filtersBackupsWithCharacter() {
        assertEquals(
            listOf("2026-11-03_12-00-05_2", "2026-11-03_12-00-05"),
            parseBackupList(listing).containing("1").map { it.name },
        )
    }
}
