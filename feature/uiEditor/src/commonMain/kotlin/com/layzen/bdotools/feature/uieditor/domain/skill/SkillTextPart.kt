package com.layzen.bdotools.feature.uieditor.domain.skill

/** Кусок текста тултипа навыка после разбора разметки ([parseSkillMarkup]). */
sealed interface SkillTextPart {

    /** Цвет куска, `0xRRGGBB`; `null` — цвет текста по умолчанию. */
    val color: Int?

    /** Обычный текст, вместе с переводами строк. */
    data class Plain(val text: String, override val color: Int? = null) : SkillTextPart

    /** Клавиша действия персонажа `{KeyBind:<action>}`: подпись берётся из его [KeyBindings]. */
    data class Key(val action: String, override val color: Int? = null) : SkillTextPart
}
