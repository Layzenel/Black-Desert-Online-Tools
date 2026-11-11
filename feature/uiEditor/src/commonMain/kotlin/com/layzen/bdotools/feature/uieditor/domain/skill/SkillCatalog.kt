package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Справочник навыков с перезарядкой по `SkillNo`. Навыков без перезарядки и части навыков хотбара в нём нет:
 * неизвестный номер — обычное дело, а не ошибка.
 *
 * @property skills навыки по `SkillNo`
 */
data class SkillCatalog(val skills: Map<Int, Skill>) {

    /** Навык с номером [skillNo] или `null`, если его нет в справочнике. */
    operator fun get(skillNo: Int): Skill? = skills[skillNo]

    /**
     * Навыки последнего ранга, которые можно поставить персонажу класса [classKey] в стойке [stance], включая общие
     * для нескольких классов: у пробуждения нет «Мощи», у наследия — навыков пробуждения. [hideAbsoluteWithPrime] —
     * у наследия ещё и без «Абсолюта», у которого есть вариант «Мощь».
     */
    fun choicesFor(classKey: String, stance: SkillStance, hideAbsoluteWithPrime: Boolean): Map<Int, Skill> =
        skills.filterValues { skill ->
            skill.max && classKey in skill.classes && skill.fitsStance(stance) &&
                !(hideAbsoluteWithPrime && stance == SkillStance.Succession && skill.hasPrimeVariant)
        }

    /**
     * Стойка персонажа по номерам его навыков: есть навык только пробуждения или только наследия — та стойка.
     * `null`, если стоят только общие навыки.
     */
    fun stanceOf(skillNos: Collection<Int>): SkillStance? =
        skillNos.firstNotNullOfOrNull { skills[it]?.stance }

    /**
     * Класс персонажа по номерам его навыков: общий класс всех известных навыков. `null`, если известных навыков
     * нет или общих классов несколько (стоят только навыки, общие для пары классов или для всех).
     */
    fun classOf(skillNos: Collection<Int>): String? {
        val known = skillNos.mapNotNull { skills[it]?.classes }
        if (known.isEmpty()) return null
        return known.reduce { common, classes -> common intersect classes }.singleOrNull()
    }
}
