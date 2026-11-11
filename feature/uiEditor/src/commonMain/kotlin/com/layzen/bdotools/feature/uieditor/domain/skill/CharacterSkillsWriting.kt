package com.layzen.bdotools.feature.uieditor.domain.skill

import com.layzen.bdotools.feature.uieditor.domain.game.attributesOf

// Так же терпимо к порядку и лишним атрибутам, как разбор: меняется только значение SkillNo
private val SLOT_LINE = Regex("""<SkillCoolTimeSlot\s[^>]*?SlotNo="(\d+)"[^>]*/>""")
private val SKILL_NO = Regex("""SkillNo="\d+"""")
private val MEMO_LINE = Regex("""[ \t]*<SkillCoolTimeMemo\s+SkillNo="\d+"[^>]*/>\r?\n?""")
private const val MEMO_BLOCK_END = "</SkillCoolTimeMemo>"
private const val SLOT_BLOCK_END = "</SkillCoolTimeSlot>"
private const val NEWLINE = "\r\n"

/**
 * Записывает в файл персонажа навыки слотов перезарядки [slots] (`SlotNo` → `SkillNo`, слоты без навыка — `0`)
 * и надписи [memos] (`SkillNo` → текст). [text] и результат — файл по символу на байт (Latin-1), надписи —
 * в Windows-1251. Меняются только нужные строки: остальные надписи и порядок строк остаются как в файле.
 */
fun writeCharacterSkills(text: String, slots: Map<Int, Int>, memos: Map<Int, String>): String =
    writeMemos(writeSlots(text, slots), memos)

private fun writeSlots(text: String, slots: Map<Int, Int>): String = SLOT_LINE.replace(text) { match ->
    val slotNo = match.groupValues[1].toInt()
    match.value.replace(SKILL_NO, "SkillNo=\"${slots[slotNo] ?: 0}\"")
}

/** Строки надписей: изменённые заменяются, лишние удаляются, новые добавляются в конец списка. */
private fun writeMemos(text: String, memos: Map<Int, String>): String {
    val written = mutableSetOf<Int>()
    val replaced = MEMO_LINE.replace(text) { match ->
        val skillNo = attributesOf(match.value)["SkillNo"]?.toIntOrNull() ?: return@replace match.value
        val memo = memos[skillNo] ?: return@replace ""
        written += skillNo
        val indent = match.value.takeWhile { it == ' ' || it == '\t' }
        val ending = match.value.substring(match.value.trimEnd('\r', '\n').length)
        indent + memoLine(skillNo, memo) + ending
    }
    val added = memos.filterKeys { it !in written }
    if (added.isEmpty()) return replaced
    val lines = added.entries.joinToString("") { (skillNo, memo) -> "\t" + memoLine(skillNo, memo) + NEWLINE }
    val end = replaced.indexOf(MEMO_BLOCK_END)
    if (end >= 0) return replaced.substring(0, end) + lines + replaced.substring(end)
    // Списка надписей в файле ещё нет: ставим его сразу за слотами перезарядки
    val slotsEnd = replaced.indexOf(SLOT_BLOCK_END).takeIf { it >= 0 } ?: return replaced
    val insertAt = slotsEnd + SLOT_BLOCK_END.length
    val block = "$NEWLINE<SkillCoolTimeMemo Version=\"1\">$NEWLINE$lines$MEMO_BLOCK_END"
    return replaced.substring(0, insertAt) + block + replaced.substring(insertAt)
}

private fun memoLine(skillNo: Int, memo: String) =
    """<SkillCoolTimeMemo SkillNo="$skillNo" Memo="${encodeCp1251(escapeXml(memo))}"/>"""

private fun escapeXml(value: String) =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
