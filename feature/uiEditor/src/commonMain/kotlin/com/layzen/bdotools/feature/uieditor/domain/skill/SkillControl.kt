package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Контроль навыка.
 *
 * @property type какой контроль
 * @property pveOnly только в PvE («Недоступно в PvP» во всех его строках тултипа)
 * @property lines номера его строк в [SkillText.effects]: у всех языков они одни и те же
 */
data class SkillControl(val type: ControlType, val pveOnly: Boolean, val lines: List<Int>)
