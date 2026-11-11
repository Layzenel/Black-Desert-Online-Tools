package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Текст тултипа навыка на одном языке, как его отдаёт сайт данных. В текстовых полях цвет задают теги
 * `<span style="color: #…">`, перевод строки — `\n`, клавиши — `{KeyBind:<действие>}` ([KeyBindings]).
 *
 * @property name название
 * @property description описание, вместе с «Исходные навыки» и «После изучения… недоступно»
 * @property requiredSkillPoints очки умений; 0 — не нужны
 * @property requiredLevel уровень персонажа; 0 — любой
 * @property requiredMp расход MP; 0 — нет
 * @property requiredSp расход SP; 0 — нет
 * @property cooldownMs перезарядка, мс
 * @property command как использовать: сочетания клавиш
 * @property effects эффекты: строки по группам, у группы бывает несколько частей
 */
data class SkillText(
    val name: String,
    val description: String,
    val requiredSkillPoints: Int,
    val requiredLevel: Int,
    val requiredMp: Int,
    val requiredSp: Int,
    val cooldownMs: Long,
    val command: String,
    val effects: List<List<String>>,
)
