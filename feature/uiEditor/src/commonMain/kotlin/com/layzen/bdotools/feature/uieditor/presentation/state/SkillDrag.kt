package com.layzen.bdotools.feature.uieditor.presentation.state

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId

/**
 * Перетаскивание навыка между слотами перезарядки (Shift+ЛКМ): откуда взят навык и где сейчас курсор.
 *
 * @property from слот, из которого тащат навык
 * @property x точка под курсором по горизонтали, UI-px от центра экрана
 * @property y то же по вертикали: над каким слотом точку отпустят, туда навык и встанет
 */
data class SkillDrag(val from: SlotId, val x: Float, val y: Float)
