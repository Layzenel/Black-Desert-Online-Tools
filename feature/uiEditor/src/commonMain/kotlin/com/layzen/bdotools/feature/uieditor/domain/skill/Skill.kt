package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Навык из справочника `bdo-tools-data`.
 *
 * @property classes ключи классов (`CharacterClass.key`), у которых он есть: бывает несколько — общие навыки
 *   пар классов и навыки всех классов; `class-<номер>` — новый класс, ещё не внесённый в справочник
 * @property names название по коду языка (`ru`, `en`)
 * @property icon путь иконки на сайте данных, от его корня
 * @property stance стойка, в которой навык доступен; `null` — в обеих (основные навыки, «Absolute:», «Flow:»)
 * @property max последний ранг навыка: только такие предлагаются при выборе умения
 * @property hasPrimeVariant навык «Абсолют», у которого в классе есть вариант наследия «Мощь»: после «Мощи» он
 *   не учится, поэтому персонажу наследия его можно спрятать
 */
data class Skill(
    val classes: Set<String>,
    val names: Map<String, String>,
    val icon: String,
    val stance: SkillStance? = null,
    val max: Boolean = false,
    val hasPrimeVariant: Boolean = false,
) {
    /** Название на языке [language]; если перевода нет — английское. */
    fun name(language: String): String = names[language] ?: names[FALLBACK_LANGUAGE].orEmpty()

    /** Доступен ли навык персонажу в стойке [stance]: общий навык — в любой. */
    fun fitsStance(stance: SkillStance): Boolean = this.stance == null || this.stance == stance

    private companion object {
        const val FALLBACK_LANGUAGE = "en"
    }
}
