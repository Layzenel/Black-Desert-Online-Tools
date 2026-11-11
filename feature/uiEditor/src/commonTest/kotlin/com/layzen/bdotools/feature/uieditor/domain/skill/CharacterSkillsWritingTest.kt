package com.layzen.bdotools.feature.uieditor.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterSkillsWritingTest {

    private val file = listOf(
        "<Other Value=\"1\"/>",
        "<SkillCoolTimeSlot Version=\"2\">",
        "\t<SkillCoolTimeSlot SlotNo=\"0\" SkillNo=\"5338\"/>",
        "\t<SkillCoolTimeSlot SlotNo=\"1\" SkillNo=\"0\"/>",
        "\t<SkillCoolTimeSlot SlotNo=\"2\" SkillNo=\"5348\"/>",
        "</SkillCoolTimeSlot>",
        "<SkillCoolTimeMemo Version=\"1\">",
        "\t<SkillCoolTimeMemo SkillNo=\"5338\" Memo=\"old\"/>",
        "\t<SkillCoolTimeMemo SkillNo=\"5348\" Memo=\"gone\"/>",
        "\t<SkillCoolTimeMemo SkillNo=\"777\" Memo=\"kept\"/>",
        "</SkillCoolTimeMemo>",
        "",
    ).joinToString("\r\n")

    @Test
    fun writesSlotsChangesMemosAndKeepsTheRest() {
        val written = writeCharacterSkills(
            file,
            slots = mapOf(1 to 900, 2 to 5348),
            memos = mapOf(5338 to "new & <x>", 777 to "kept", 900 to "ЛКМ"),
        )

        assertEquals(
            listOf(
                "<Other Value=\"1\"/>",
                "<SkillCoolTimeSlot Version=\"2\">",
                "\t<SkillCoolTimeSlot SlotNo=\"0\" SkillNo=\"0\"/>",
                "\t<SkillCoolTimeSlot SlotNo=\"1\" SkillNo=\"900\"/>",
                "\t<SkillCoolTimeSlot SlotNo=\"2\" SkillNo=\"5348\"/>",
                "</SkillCoolTimeSlot>",
                "<SkillCoolTimeMemo Version=\"1\">",
                "\t<SkillCoolTimeMemo SkillNo=\"5338\" Memo=\"new &amp; &lt;x&gt;\"/>",
                "\t<SkillCoolTimeMemo SkillNo=\"777\" Memo=\"kept\"/>",
                "\t<SkillCoolTimeMemo SkillNo=\"900\" Memo=\"${encodeCp1251("ЛКМ")}\"/>",
                "</SkillCoolTimeMemo>",
                "",
            ).joinToString("\r\n"),
            written,
        )
    }

    @Test
    fun writtenFileReadsBackTheSame() {
        val slots = mapOf(0 to 5338, 2 to 5348)
        val memos = mapOf(5338 to "Ёж № 1", 5348 to "Sh+F")

        val skills = parseCharacterSkills("1", writeCharacterSkills(file, slots, memos))

        assertEquals(CharacterSkills("1", slots, memos), skills)
    }

    @Test
    fun slotLineWithOtherAttributeOrderIsWritten() {
        val odd = "\t<SkillCoolTimeSlot SlotNo=\"3\" Extra=\"1\" SkillNo=\"7\" />\r\n"

        assertEquals("\t<SkillCoolTimeSlot SlotNo=\"3\" Extra=\"1\" SkillNo=\"42\" />\r\n", writeCharacterSkills(odd, mapOf(3 to 42), emptyMap()))
    }

    @Test
    fun addsMemoBlockWhenFileHasNone() {
        val noMemos = "<SkillCoolTimeSlot Version=\"2\">\r\n\t<SkillCoolTimeSlot SlotNo=\"0\" SkillNo=\"5\"/>\r\n</SkillCoolTimeSlot>\r\n"

        val written = writeCharacterSkills(noMemos, mapOf(0 to 5), mapOf(5 to "Q"))

        assertTrue("<SkillCoolTimeMemo Version=\"1\">\r\n\t<SkillCoolTimeMemo SkillNo=\"5\" Memo=\"Q\"/>\r\n</SkillCoolTimeMemo>" in written)
        assertEquals(mapOf(5 to "Q"), parseCharacterSkills("1", written).memos)
    }

    @Test
    fun cp1251RoundTripAndUnsupportedCharacters() {
        val text = "Привет, Ёё №5 «x» — €"

        assertEquals(text, decodeCp1251(encodeCp1251(text)))
        assertEquals("a?b", decodeCp1251(encodeCp1251("a漢b")))
        assertEquals(false, isCp1251('漢'))
        assertEquals(true, isCp1251('Ж'))
    }
}
