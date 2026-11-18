package com.layzen.bdotools.feature.comboassist.domain.skill

/**
 * Умения класса для комбо. Неизвестный номер — умение без перезарядки и связок: справочник бывает неполным.
 *
 * @property bySkillNo умения по номеру навыка
 */
class ComboSkills(val bySkillNo: Map<Int, ComboSkill>) {

    /** Умение [skillNo] или пустое описание, если его нет в справочнике. */
    operator fun get(skillNo: Int): ComboSkill = bySkillNo[skillNo] ?: ComboSkill(skillNo)

    /** Перезарядка умения [skillNo] в миллисекундах. */
    fun cooldown(skillNo: Int): Long = this[skillNo].cooldownMs

    /** Умения, которые можно применить сразу после [skillNo]: сначала продолжения связок. */
    fun followUpsOf(skillNo: Int): List<Int> = bySkillNo.values
        .filter { skillNo in it.after }
        .sortedWith(compareByDescending<ComboSkill> { it.onlyAfter }.thenBy { it.skillNo })
        .map { it.skillNo }

    /** Можно ли применить [skillNo] после умений [previous]: продолжение связки — только после своего запуска. */
    fun canFollow(skillNo: Int, previous: Collection<Int>): Boolean {
        val skill = this[skillNo]
        return !skill.onlyAfter || previous.any { it in skill.after }
    }

    companion object {
        /** Пустой набор: пока справочник не загружен. */
        val Empty = ComboSkills(emptyMap())
    }
}
