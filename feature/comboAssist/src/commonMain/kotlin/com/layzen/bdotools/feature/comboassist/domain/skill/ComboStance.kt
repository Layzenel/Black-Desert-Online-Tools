package com.layzen.bdotools.feature.comboassist.domain.skill

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.skill.domain.Skill
import com.layzen.bdotools.core.skill.domain.SkillStance

/**
 * Стойка, умения которой редактор комбо предлагает. У обычных классов — наследие и пробуждение (по стойке навыка
 * в справочнике), у Серафима — Сакраментум и Пургатум: их навыки различаются только приставкой названия
 * («С:»/«Sacra:» и «П:»/«Purga:»), а в справочнике стойки у них нет.
 *
 * @property skillStance стойка навыков справочника, которые подходят (`null` — справочник не различает)
 * @property hiddenPrefixes начала названий (на любом языке) навыков другой стойки, которые прячутся
 */
enum class ComboStance(val skillStance: SkillStance?, val hiddenPrefixes: List<String> = emptyList()) {
    Succession(SkillStance.Succession),
    Awakening(SkillStance.Awakening),
    Sacramentum(null, listOf("П:", "Purga:")),
    Purgatum(null, listOf("С:", "Sacra:"));

    /**
     * Можно ли поставить навык [skill] в этой стойке: навык нужной стойки справочника или общий, не другой стойки
     * Серафима; у наследия ещё и без «Абсолюта», у которого есть вариант «Мощь» (после «Мощи» он не учится).
     */
    fun allows(skill: Skill): Boolean =
        (skillStance == null || skill.fitsStance(skillStance)) &&
            !(this == Succession && skill.hasPrimeVariant) &&
            hiddenPrefixes.none { prefix -> skill.names.values.any { it.startsWith(prefix) } }

    companion object {
        /** Стойки класса [characterClass] на выбор; пусто — у класса один набор навыков. */
        fun of(characterClass: CharacterClass): List<ComboStance> = when {
            characterClass == CharacterClass.Seraph -> listOf(Sacramentum, Purgatum)
            characterClass.ascension -> emptyList()
            else -> listOf(Succession, Awakening)
        }

        /** Стойка при смене класса на [characterClass]: прежняя [current], если она у него есть (или все), иначе первая. */
        fun keep(characterClass: CharacterClass, current: ComboStance?): ComboStance? {
            val options = of(characterClass)
            return when {
                options.isEmpty() -> null
                current == null || current in options -> current
                else -> options.first()
            }
        }
    }
}
