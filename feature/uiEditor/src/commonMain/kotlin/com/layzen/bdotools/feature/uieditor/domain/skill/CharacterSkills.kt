package com.layzen.bdotools.feature.uieditor.domain.skill

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId

/**
 * Навыки в слотах перезарядки одного персонажа и надписи к ним — из его `gamevariable.xml`.
 *
 * @property characterNo номер персонажа (`CharacterNo`, имя папки его файла)
 * @property slots `SkillNo` по номеру слота перезарядки `SlotNo` (0..19 — «Слот 1..20»); пустых слотов нет
 * @property memos надписи по `SkillNo`: надпись принадлежит навыку, а не слоту
 * @property keys клавиши действий персонажа для команд навыков в тултипе
 */
data class CharacterSkills(
    val characterNo: String,
    val slots: Map<Int, Int>,
    val memos: Map<Int, String>,
    val keys: KeyBindings = KeyBindings(emptyMap()),
)

/** Навыки персонажа по слотам редактора: `SlotNo` 0..19 — слоты перезарядки 1..20. */
val CharacterSkills.cooldownSkills: Map<SlotId, Int>
    get() = slots.mapKeys { (slotNo, _) -> SlotId(SlotGroup.Cooldown, slotNo + 1) }

/** `SlotNo` в файле персонажа по слотам перезарядки редактора [skills]. */
fun slotNumbersOf(skills: Map<SlotId, Int>): Map<Int, Int> =
    skills.filterKeys { it.group == SlotGroup.Cooldown }.mapKeys { (id, _) -> id.number - 1 }
