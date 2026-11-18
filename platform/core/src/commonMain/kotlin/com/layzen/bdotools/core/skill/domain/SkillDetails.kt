package com.layzen.bdotools.core.skill.domain

/**
 * Подробности навыка для тултипа: текст на каждом языке сайта данных.
 *
 * @property texts текст по коду языка (`ru`, `en`)
 */
data class SkillDetails(val texts: Map<String, SkillText>) {

    /** Текст на языке [language]; если перевода нет — английский. */
    fun text(language: String): SkillText? = texts[language] ?: texts[FALLBACK_LANGUAGE]

    private companion object {
        const val FALLBACK_LANGUAGE = "en"
    }
}
