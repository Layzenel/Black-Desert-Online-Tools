package com.layzen.bdotools.feature.comboassist.domain

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkill
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills

/** Умения Страйкера (наследие) с настоящими перезарядками и связками bdolytics и пример комбо из прототипа. */
object StrikerFixture {
    const val WOLF_FANG = 5358
    const val RAGE_HAMMER = 5359
    const val RAMPAGING = 5361
    const val ADAMANTINE = 5356
    const val CRIMSON_FANG = 5348
    const val DEATHSTRIKE = 2553
    const val BOMBARDMENT = 2554
    const val ROARING_TIGER = 5357
    const val KNEE_HAMMER = 5338
    const val HEART_AND_SOUL = 5372
    const val SOMERSAULT = 5341
    const val SUPPRESSION = 3042
    const val CROUCHING_WOLF = 2481
    const val ANKLE_HOOK = 3035
    const val MARTIAL_DEVA = 3036

    val skills = ComboSkills(
        listOf(
            ComboSkill(WOLF_FANG, 8000), ComboSkill(RAGE_HAMMER, 8000), ComboSkill(RAMPAGING, 8000),
            ComboSkill(ADAMANTINE, 4000), ComboSkill(CRIMSON_FANG, 4000),
            ComboSkill(DEATHSTRIKE, 3000, setOf(CRIMSON_FANG, ADAMANTINE), onlyAfter = true),
            ComboSkill(BOMBARDMENT, 3000, setOf(DEATHSTRIKE, MARTIAL_DEVA), onlyAfter = true),
            ComboSkill(ROARING_TIGER, 5000), ComboSkill(KNEE_HAMMER, 5000), ComboSkill(HEART_AND_SOUL, 8000),
            ComboSkill(SOMERSAULT, 8000), ComboSkill(SUPPRESSION, 15000), ComboSkill(CROUCHING_WOLF, 5000),
            ComboSkill(ANKLE_HOOK, 5000, setOf(ADAMANTINE)),
            ComboSkill(MARTIAL_DEVA, 5000, setOf(ANKLE_HOOK), onlyAfter = true),
        ).associateBy { it.skillNo },
    )

    private fun cell(no: Int, chain: Boolean = false) = ComboCell(no, chain)

    /** Развилка из трёх веток, связка Алый коготь → Удар под дых → Рубящая длань, развилка 2×2, два умения в пуле. */
    val demo = Combo(
        id = "demo",
        name = "Пример",
        classKey = "striker",
        fillers = listOf(ANKLE_HOOK),
        pool = listOf(SUPPRESSION, CROUCHING_WOLF),
        columns = listOf(
            ComboColumn.Fork(listOf(listOf(cell(WOLF_FANG)), listOf(cell(RAGE_HAMMER)), listOf(cell(RAMPAGING)))),
            ComboColumn.Single(cell(ADAMANTINE)),
            ComboColumn.Single(cell(CRIMSON_FANG)),
            ComboColumn.Single(cell(DEATHSTRIKE, chain = true)),
            ComboColumn.Single(cell(BOMBARDMENT, chain = true)),
            ComboColumn.Fork(listOf(listOf(cell(ROARING_TIGER), cell(KNEE_HAMMER)), listOf(cell(HEART_AND_SOUL), cell(SOMERSAULT)))),
        ),
    )
}
