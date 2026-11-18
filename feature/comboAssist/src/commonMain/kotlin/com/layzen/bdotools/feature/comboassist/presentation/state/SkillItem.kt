package com.layzen.bdotools.feature.comboassist.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.skill.domain.Skill
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillTraits

/**
 * Умение класса для редактора и оверлея.
 *
 * @property skillNo номер навыка
 * @property skill запись справочника: названия, перезарядка, связки
 * @property icon иконка или `null`, пока она не скачалась
 * @property traits урон и клавиши из подробностей; `null` — подробности ещё не скачались
 */
data class SkillItem(val skillNo: Int, val skill: Skill, val icon: ImageBitmap? = null, val traits: SkillTraits? = null) {
    /** Наносит ли урон (для списка филлеров); `null` — ещё неизвестно. */
    val damage: Boolean? get() = traits?.damage
}
