package com.layzen.bdotools.feature.comboassist.domain.history

/**
 * Слот перезарядки на экране игры.
 *
 * @property skillNo умение в слоте
 * @property centerX центр по горизонтали в долях ширины экрана
 * @property centerY центр по вертикали в долях высоты экрана
 * @property size сторона слота в долях высоты экрана
 */
data class ScreenSlot(val skillNo: Int, val centerX: Double, val centerY: Double, val size: Double)
