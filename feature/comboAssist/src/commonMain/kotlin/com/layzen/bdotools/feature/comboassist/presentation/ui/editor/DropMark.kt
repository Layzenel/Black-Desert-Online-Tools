package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.layzen.bdotools.feature.comboassist.domain.combo.DropSide
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Голубой указатель снаружи ячейки на стороне [side]: треугольник основанием во всю сторону ячейки, остриём от неё —
 * в промежуток, куда встанет перетаскиваемое умение. [modifier] — размер ячейки.
 */
@Composable
internal fun DropMark(side: DropSide, modifier: Modifier) {
    Box(modifier) {
        val arrow = ComboDimens.dropArrow
        val place = when (side) {
            DropSide.Before -> Modifier.align(Alignment.CenterStart).offset(x = -arrow).width(arrow).fillMaxHeight()
            DropSide.After -> Modifier.align(Alignment.CenterEnd).offset(x = arrow).width(arrow).fillMaxHeight()
            DropSide.Above -> Modifier.align(Alignment.TopCenter).offset(y = -arrow).height(arrow).fillMaxWidth()
            DropSide.Below -> Modifier.align(Alignment.BottomCenter).offset(y = arrow).height(arrow).fillMaxWidth()
        }
        val color = AppTheme.colors.accentBlue
        Canvas(place) {
            val w = size.width
            val h = size.height
            // основание — у края ячейки, остриё — наружу
            val (tip, baseStart, baseEnd) = when (side) {
                DropSide.Before -> Triple(Offset(0f, h / 2), Offset(w, 0f), Offset(w, h))
                DropSide.After -> Triple(Offset(w, h / 2), Offset(0f, 0f), Offset(0f, h))
                DropSide.Above -> Triple(Offset(w / 2, 0f), Offset(0f, h), Offset(w, h))
                DropSide.Below -> Triple(Offset(w / 2, h), Offset(0f, 0f), Offset(w, 0f))
            }
            drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(baseStart.x, baseStart.y); lineTo(baseEnd.x, baseEnd.y); close() }, color)
        }
    }
}
