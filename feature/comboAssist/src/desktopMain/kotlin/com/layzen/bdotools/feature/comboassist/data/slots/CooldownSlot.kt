package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Слот перезарядки умения на экране игры.
 *
 * @property skillNo умение в слоте
 * @property centerX центр слота по горизонтали в долях экрана (`RelativePosX` элемента)
 * @property centerY центр слота по вертикали в долях экрана (`RelativePosY` элемента)
 */
internal data class CooldownSlot(val skillNo: Int, val centerX: Double, val centerY: Double)
