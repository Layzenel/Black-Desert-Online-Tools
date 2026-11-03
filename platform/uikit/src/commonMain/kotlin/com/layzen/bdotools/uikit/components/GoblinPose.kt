package com.layzen.bdotools.uikit.components

import androidx.compose.ui.geometry.Offset

/**
 * Положение частей гоблина в один момент бега. Координаты — в квадрате [GOBLIN_BOX] × [GOBLIN_BOX],
 * гоблин смотрит вправо. Конечности — цепочки точек: бедро → колено → лодыжка → носок,
 * плечо → локоть → кисть. Первая нога и первая рука — дальние.
 */
internal data class GoblinPose(
    val hip: Offset,
    val shoulder: Offset,
    val belly: Offset,
    val head: Offset,
    val legs: List<List<Offset>>,
    val arms: List<List<Offset>>,
    val ear: List<Offset>,
    val nose: List<Offset>,
)
