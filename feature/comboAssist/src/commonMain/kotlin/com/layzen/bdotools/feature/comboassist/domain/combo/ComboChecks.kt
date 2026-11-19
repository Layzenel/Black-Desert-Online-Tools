package com.layzen.bdotools.feature.comboassist.domain.combo

import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills

/**
 * Продолжения связок, которые стоят не после своего запуска: по данным умение применяется только после
 * определённых умений, а перед ячейкой их нет. Данные bdolytics бывают неполными — это предупреждение, не запрет.
 */
fun Combo.chainProblems(skills: ComboSkills): List<CellAt> = cells().mapNotNull { (at, cell) ->
    val skillNo = cell.skillNo ?: return@mapNotNull null
    at.takeIf { !skills.canFollow(skillNo, previousSkills(at)) }
}

/** Сколько в комбо пустых ячеек: оверлей их пропускает. */
fun Combo.emptyCells(): Int = cells().count { it.second.skillNo == null }

