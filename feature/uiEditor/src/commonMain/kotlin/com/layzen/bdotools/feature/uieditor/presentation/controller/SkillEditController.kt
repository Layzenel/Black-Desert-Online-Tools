package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotBounds
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillStance
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillDrag
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.skill.isCp1251
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState

/**
 * Правка навыков слотов перезарядки и их надписей в редакторе: окно надписи и выбор навыка класса.
 * Правки идут в раскладку и отменяются, как остальные; в файл персонажа их пишет «Применить».
 */
internal class SkillEditController(
    private val context: SlotsContext,
    private val skills: SkillsController,
) {
    /** Окно надписи навыка в слоте [id]; у пустого слота надписи навыка нет. */
    fun openMemo(id: SlotId) {
        val skillNo = context.current.layout.skillAt(id) ?: return
        val memo = context.current.layout.memos?.get(skillNo)
        context.update { it.copy(dialog = SlotsDialog.EditMemo(skillNo, memo.orEmpty(), existing = memo != null)) }
    }

    /** Текст надписи: не длиннее, чем позволяет игра, и только знаки, которые она покажет (Windows-1251). */
    fun changeMemo(text: String) = context.update {
        val dialog = it.dialog as? SlotsDialog.EditMemo ?: return@update it
        it.copy(dialog = dialog.copy(text = text.filter(::isCp1251).take(SlotLabel.MAX_CHARS)))
    }

    /** Ставит надпись из окна; пустая убирает надпись. */
    fun confirmMemo() {
        val dialog = context.current.dialog as? SlotsDialog.EditMemo ?: return
        context.editLayout { it.withMemo(dialog.skillNo, dialog.text.trim()) }
        context.update { it.copy(dialog = null) }
    }

    /** Убирает надпись навыка в слоте [id]. */
    fun deleteMemo(id: SlotId) {
        val skillNo = context.current.layout.skillAt(id) ?: return
        context.editLayout { it.withMemo(skillNo, null) }
    }

    /**
     * Выбор навыка для слота [id]: навыки последнего ранга класса и стойки, которые сейчас в редакторе
     * ([SlotsState.skillsClass], [SlotsState.skillsStance]). Класс не определить — выбирать не из чего.
     */
    fun openSkillPicker(id: SlotId) {
        val state = context.current
        if (state.skills.catalog == null) return
        val classKey = state.skillsClass ?: return
        // Стойка — по навыкам в слотах; стоят только общие — наследие, его выбирают чаще
        context.update { it.copy(dialog = SlotsDialog.PickSkill(id, classKey, state.skillsStance ?: SkillStance.Succession)) }
        loadChoiceData()
    }

    /** Строка поиска навыка по названию. */
    fun changeQuery(query: String) = updatePicker { it.copy(query = query) }

    /** Навыки другой стойки в окне выбора. */
    fun changeStance(stance: SkillStance) {
        updatePicker { it.copy(stance = stance) }
        loadChoiceData()
    }

    /** Прятать ли у наследия «Абсолют», у которого есть вариант «Мощь». */
    fun changeHideAbsolute(hide: Boolean) = updatePicker { it.copy(hideAbsolute = hide) }

    /** Прятать ли на плашках навыков контроли, которые недоступны в PvP. */
    fun changeHidePveControls(hide: Boolean) = updatePicker { it.copy(hidePveControls = hide) }

    // Подробности нужны плашкам эффектов у всех навыков списка, а не только тултипу
    private fun loadChoiceData() {
        val dialog = context.current.dialog as? SlotsDialog.PickSkill ?: return
        val choices = context.current.skills.catalog?.choicesFor(dialog.classKey, dialog.filterStance, hideAbsoluteWithPrime = false)
            ?.keys.orEmpty()
        skills.loadIcons(choices)
        choices.forEach(skills::loadDetails)
    }

    /** Ставит навык [skillNo] в слот, для которого открыт выбор. */
    fun pickSkill(skillNo: Int) {
        val dialog = context.current.dialog as? SlotsDialog.PickSkill ?: return
        context.editLayout { it.withSkill(dialog.slot, skillNo) }
        context.update { it.copy(dialog = null) }
        skills.loadIcons()
    }

    /** Очищает слот перезарядки [id]. */
    fun removeSkill(id: SlotId) = context.editLayout { it.withSkill(id, null) }

    /** Начинает перетаскивание навыка из слота [id] (Shift+ЛКМ); пустой слот тащить нечего. */
    fun startSkillDrag(id: SlotId) {
        val state = context.current
        if (state.layout.skillAt(id) == null) return
        val center = state.layout[id] ?: return
        context.update { it.copy(skillDrag = SkillDrag(id, center.x.toFloat(), center.y.toFloat())) }
    }

    /** Ведёт навык за курсором на [dx], [dy] UI-px. */
    fun skillDragBy(dx: Float, dy: Float) = context.update {
        val drag = it.skillDrag ?: return@update it
        it.copy(skillDrag = drag.copy(x = drag.x + dx, y = drag.y + dy))
    }

    /**
     * Отпускает навык: в слот перезарядки под курсором он встаёт, а навык оттуда — на его место. Мимо слотов —
     * ничего не меняется. shortcut: точка — центр взятого слота плюс сдвиг курсора, без места хвата внутри рамки
     * (до половины слота); считать от точки нажатия, если будут промахи.
     */
    fun endSkillDrag() {
        val state = context.current
        val drag = state.skillDrag ?: return
        context.update { it.copy(skillDrag = null) }
        val target = state.slots.firstOrNull { id ->
            val position = state.layout[id] ?: return@firstOrNull false
            val bounds = SlotBounds.of(id, position)
            id.group == SlotGroup.Cooldown && drag.x.toDouble() in bounds.left..bounds.right && drag.y.toDouble() in bounds.top..bounds.bottom
        } ?: return
        context.editLayout { it.withSkillMoved(drag.from, target) }
    }

    private fun updatePicker(change: (SlotsDialog.PickSkill) -> SlotsDialog.PickSkill) = context.update {
        val dialog = it.dialog as? SlotsDialog.PickSkill ?: return@update it
        it.copy(dialog = change(dialog))
    }
}
