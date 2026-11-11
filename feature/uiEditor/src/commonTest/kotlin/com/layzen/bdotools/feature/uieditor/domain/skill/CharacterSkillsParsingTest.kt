package com.layzen.bdotools.feature.uieditor.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals

class CharacterSkillsParsingTest {

    /** Строка cp1251-байтов, прочитанная по символу на байт, — как файл игры отдаёт хранилище. */
    private fun latin1(vararg bytes: Int) = bytes.joinToString("") { it.toChar().toString() }

    private val memo = latin1(0xCB, 0xCA, 0xCC) + "+" + latin1(0xCF, 0xCA, 0xCC) // «ЛКМ+ПКМ» в cp1251

    private val file = """
        |<SkillCoolTimeSlot Version="2">
        |	<SkillCoolTimeSlot SlotNo="0" SkillNo="5338"/>
        |	<SkillCoolTimeSlot SlotNo="1" SkillNo="0"/>
        |	<SkillCoolTimeSlot SlotNo="19" SkillNo="5348"/>
        |</SkillCoolTimeSlot>
        |<SkillCoolTimeMemo Version="1">
        |	<SkillCoolTimeMemo SkillNo="5338" Memo="$memo"/>
        |	<SkillCoolTimeMemo SkillNo="5348" Memo="Sh+F &amp; Q"/>
        |	<SkillCoolTimeMemo SkillNo="77" Memo=""/>
        |</SkillCoolTimeMemo>
    """.trimMargin().replace("\n", "\r\n")

    @Test
    fun readsFilledSlotsAndSkipsEmpty() {
        val skills = parseCharacterSkills("4000", file)

        assertEquals("4000", skills.characterNo)
        assertEquals(mapOf(0 to 5338, 19 to 5348), skills.slots)
    }

    @Test
    fun decodesCp1251MemosAndXmlEntities() {
        val memos = parseCharacterSkills("4000", file).memos

        assertEquals(mapOf(5338 to "ЛКМ+ПКМ", 5348 to "Sh+F & Q"), memos)
    }

    @Test
    fun decodesWholeCp1251Range() {
        assertEquals("Ёё№ЂїАЯая", decodeCp1251(latin1(0xA8, 0xB8, 0xB9, 0x80, 0xBF, 0xC0, 0xDF, 0xE0, 0xFF)))
        assertEquals("Fire", decodeCp1251("Fire"))
    }

    @Test
    fun fileWithoutSkillsGivesEmptyCharacter() {
        assertEquals(CharacterSkills("1", emptyMap(), emptyMap()), parseCharacterSkills("1", "<GameVariable/>"))
    }
}
