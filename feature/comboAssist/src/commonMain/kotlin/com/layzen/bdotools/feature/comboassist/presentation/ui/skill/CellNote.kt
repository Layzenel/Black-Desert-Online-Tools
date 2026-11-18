package com.layzen.bdotools.feature.comboassist.presentation.ui.skill

/**
 * Ячейка комбо для подсказки.
 *
 * @property label подпись ячейки («4.1»)
 * @property chain связка с предыдущим шагом
 * @property optional опциональный шаг
 */
internal data class CellNote(val label: String, val chain: Boolean, val optional: Boolean)
