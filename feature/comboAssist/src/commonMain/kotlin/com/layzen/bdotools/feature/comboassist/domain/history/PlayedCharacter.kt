package com.layzen.bdotools.feature.comboassist.domain.history

/**
 * Персонаж, которым сейчас играют, — узнан по слотам перезарядок на экране.
 *
 * @property slots его слоты перезарядок
 * @property classKey его класс (`CharacterClass.key`) по умениям в слотах или `null`, если не понять
 * @property historyShown видна ли у него «История испол. навыков»: без неё трекер ведёт комбо плохо
 */
data class PlayedCharacter(val slots: List<ScreenSlot>, val classKey: String?, val historyShown: Boolean = true)
