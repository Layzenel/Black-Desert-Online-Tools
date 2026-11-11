package com.layzen.bdotools.feature.uieditor.domain.skill

import com.layzen.bdotools.feature.uieditor.domain.game.attributesOf

private val SLOT = Regex("""<SkillCoolTimeSlot\s+SlotNo="\d+"[^>]*/>""")
private val MEMO = Regex("""<SkillCoolTimeMemo\s+SkillNo="\d+"[^>]*/>""")

/**
 * Читает навыки слотов перезарядки и надписи из файла персонажа [characterNo]. [text] — файл, прочитанный
 * по символу на байт (Latin-1), как его читает хранилище: надписи в нём в Windows-1251.
 */
fun parseCharacterSkills(characterNo: String, text: String): CharacterSkills {
    val slots = SLOT.findAll(text).mapNotNull { match ->
        val attributes = attributesOf(match.value)
        val slotNo = attributes["SlotNo"]?.toIntOrNull()
        val skillNo = attributes["SkillNo"]?.toIntOrNull()?.takeIf { it != 0 }
        if (slotNo == null || skillNo == null) null else slotNo to skillNo
    }.toMap()
    val memos = MEMO.findAll(text).mapNotNull { match ->
        val attributes = attributesOf(match.value)
        val skillNo = attributes["SkillNo"]?.toIntOrNull() ?: return@mapNotNull null
        val memo = attributes["Memo"]?.let { unescapeXml(decodeCp1251(it)) }?.takeIf { it.isNotBlank() }
            ?: return@mapNotNull null
        skillNo to memo
    }.toMap()
    return CharacterSkills(characterNo, slots, memos, parseKeyBindings(text))
}

private val XML_ENTITIES = listOf("&lt;" to "<", "&gt;" to ">", "&quot;" to "\"", "&apos;" to "'", "&amp;" to "&")

/** Значение атрибута XML как текст: `&amp;` → `&` и т. п. (`&amp;` — последним, чтобы не раскрыть дважды). */
private fun unescapeXml(value: String): String = XML_ENTITIES.fold(value) { text, (entity, char) -> text.replace(entity, char) }
