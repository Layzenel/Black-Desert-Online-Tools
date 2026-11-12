package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Эффект защиты навыка.
 *
 * @property effect какой эффект
 * @property lines номера его строк в [SkillText.effects]: у всех языков они одни и те же
 */
data class SkillDefense(val effect: DefenseEffect, val lines: List<Int>)
