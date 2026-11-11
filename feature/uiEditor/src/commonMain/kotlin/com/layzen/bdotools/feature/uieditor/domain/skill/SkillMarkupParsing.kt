package com.layzen.bdotools.feature.uieditor.domain.skill

private val TOKEN = Regex("""<span style="color:\s*#([0-9a-fA-F]{6})">|</span>|\{KeyBind:(\w+)\}""")

/**
 * Разбирает текст тултипа навыка: `<span style="color: #…">` красит кусок, `{KeyBind:<action>}` — клавиша.
 * Остальные угловые скобки (`<Regular Ammo>`) — обычный текст. Вложенные теги и непарный `</span>` не ломают
 * разбор: закрывающий тег возвращает предыдущий цвет.
 */
fun parseSkillMarkup(text: String): List<SkillTextPart> {
    val parts = mutableListOf<SkillTextPart>()
    val colors = ArrayDeque<Int>()
    var start = 0
    for (match in TOKEN.findAll(text)) {
        if (match.range.first > start) parts += SkillTextPart.Plain(text.substring(start, match.range.first), colors.lastOrNull())
        val (color, action) = match.destructured
        when {
            color.isNotEmpty() -> colors.addLast(color.toInt(16))
            action.isNotEmpty() -> parts += SkillTextPart.Key(action, colors.lastOrNull())
            else -> colors.removeLastOrNull()
        }
        start = match.range.last + 1
    }
    if (start < text.length) parts += SkillTextPart.Plain(text.substring(start), colors.lastOrNull())
    return parts
}
