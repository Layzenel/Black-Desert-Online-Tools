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

/**
 * Связанные развилки, у которых веток не столько же, сколько у предыдущей развилки: где ветки не совпадут,
 * подсказка выберет ветку по перезарядкам. Номера колонок.
 */
fun Combo.linkedForkProblems(): List<Int> {
    var previous: ComboColumn.Fork? = null
    return columns.withIndex().mapNotNull { (index, column) ->
        val fork = column as? ComboColumn.Fork ?: return@mapNotNull null
        val problem = index.takeIf { fork.linked && previous != null && previous!!.lanes.size != fork.lanes.size }
        previous = fork
        problem
    }
}
