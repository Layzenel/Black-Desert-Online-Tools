package com.layzen.bdotools.feature.comboassist.presentation.controller

import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboEdit
import com.layzen.bdotools.feature.comboassist.domain.combo.addLane
import com.layzen.bdotools.feature.comboassist.domain.combo.chainProblems
import com.layzen.bdotools.feature.comboassist.domain.combo.emptyCells
import com.layzen.bdotools.feature.comboassist.domain.combo.fill
import com.layzen.bdotools.feature.comboassist.domain.combo.growLane
import com.layzen.bdotools.feature.comboassist.domain.combo.insertCell
import com.layzen.bdotools.feature.comboassist.domain.combo.toggleLinked
import com.layzen.bdotools.feature.comboassist.domain.combo.insertColumn
import com.layzen.bdotools.feature.comboassist.domain.combo.previousSkills
import com.layzen.bdotools.feature.comboassist.domain.combo.put
import com.layzen.bdotools.feature.comboassist.domain.combo.removeCell
import com.layzen.bdotools.feature.comboassist.domain.combo.swap
import com.layzen.bdotools.feature.comboassist.domain.combo.toFork
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState

/** Правка колонок комбо: событие редактора → новое состояние. Сохранение — в ViewModel. */
internal class EditorController {

    /** Применяет событие редактора [event] к [state]; события не редактора возвращают состояние как есть. */
    fun reduce(state: ComboState, event: ComboEvent, skills: ComboSkills): ComboState {
        val combo = state.combo ?: return state
        val at = state.selection
        val edit: ComboEdit = when (event) {
            is ComboEvent.SelectCell -> return select(state, event.at, skills)
            is ComboEvent.Fill -> if (at != null) combo.fill(at, event.skillNo, skills) else {
                // ничего не выбрано — умение встаёт новой колонкой в конец
                val appended = combo.insertColumn(combo.columns.size)
                appended.combo.fill(CellAt(combo.columns.size), event.skillNo, skills)
            }
            is ComboEvent.InsertColumn -> combo.insertColumn(event.index)
            is ComboEvent.ToFork -> combo.toFork(event.column)
            is ComboEvent.AddLane -> combo.addLane(event.column)
            is ComboEvent.GrowLane -> combo.growLane(event.column, event.lane)
            is ComboEvent.ToggleLinked -> combo.toggleLinked(event.column, at)
            is ComboEvent.InsertCell -> at?.let { combo.insertCell(it, event.after) } ?: return state
            is ComboEvent.ClearSelection -> return refresh(state.copy(selection = null), skills)
            is ComboEvent.ToggleChain -> at?.let { a -> combo[a]?.let { ComboEdit(combo.put(a, it.copy(chain = !it.chain)), a) } } ?: return state
            is ComboEvent.ToggleOptional -> at?.let { a -> combo[a]?.let { ComboEdit(combo.put(a, it.copy(optional = !it.optional)), a) } } ?: return state
            is ComboEvent.RemoveCell -> at?.let { combo.removeCell(it) } ?: return state
            is ComboEvent.RemoveAt -> combo.removeCell(event.at)
            is ComboEvent.SwapCells -> combo.swap(event.from, event.to)
            is ComboEvent.AddFiller -> ComboEdit(combo.copy(fillers = (combo.fillers + event.skillNo).distinct()), at)
            is ComboEvent.RemoveFiller -> ComboEdit(combo.copy(fillers = combo.fillers - event.skillNo), at)
            else -> return state
        }
        return refresh(state.copy(combo = edit.combo, selection = edit.selection, dirty = true), skills)
    }

    /** Пересчитывает подсказки «что дальше» и предупреждения после правки или смены справочника. */
    fun refresh(state: ComboState, skills: ComboSkills): ComboState {
        val combo = state.combo ?: return state.copy(suggestions = emptyList(), problems = emptyList(), emptyCells = 0)
        val base = state.selection?.let { at -> combo[at]?.skillNo ?: combo.previousSkills(at).firstOrNull() }
        return state.copy(
            suggestions = base?.let { skills.followUpsOf(it) }.orEmpty().filter { it in state.skills },
            problems = combo.chainProblems(skills),
            emptyCells = combo.emptyCells(),
        )
    }

    private fun select(state: ComboState, at: CellAt, skills: ComboSkills): ComboState = refresh(state.copy(selection = at), skills)
}
