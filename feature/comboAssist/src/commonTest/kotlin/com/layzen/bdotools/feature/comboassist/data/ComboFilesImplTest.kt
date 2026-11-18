package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ComboFilesImplTest {
    /** Файлы в памяти: сохранённое можно тут же открыть. */
    private class MemoryFiles(var text: String? = null, var name: String? = null) : DeviceTextFiles {
        override suspend fun open(): String? = text
        override suspend fun save(fileName: String, text: String) { name = fileName; this.text = text }
    }

    private val combo = Combo(
        "a1", "Loop: PvE", "dosa",
        columns = listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1)), listOf(ComboCell(2)))), ComboColumn.Fork(listOf(listOf(ComboCell(3)), listOf(ComboCell(4))), linked = true)),
        fillers = listOf(5),
    )

    @Test
    fun exportedComboImportsBack() = runTest {
        val device = MemoryFiles()
        val files = ComboFilesImpl(device)
        files.export(combo)
        assertEquals("Loop_ PvE.json", device.name)
        assertEquals(listOf(combo), files.import())
    }

    @Test
    fun closedDialogIsNull() = runTest {
        assertNull(ComboFilesImpl(MemoryFiles()).import())
    }

    @Test
    fun otherFileIsRejected() = runTest {
        assertFailsWith<IllegalArgumentException> { ComboFilesImpl(MemoryFiles(text = "{\"combos\": 42}")).import() }
    }
}
