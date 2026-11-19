package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.comboassist.domain.combo.DropSide
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Синяя черта снаружи ячейки на стороне [side], в промежутке до соседней: туда встанет перетаскиваемое умение.
 * [modifier] — размер ячейки.
 */
@Composable
internal fun DropMark(side: DropSide, modifier: Modifier) {
    Box(modifier) {
        val out = ComboDimens.dropMarkOutset
        val mark = when (side) {
            DropSide.Before -> Modifier.align(Alignment.CenterStart).offset(x = -out).fillMaxHeight().width(ComboDimens.dropMark)
            DropSide.After -> Modifier.align(Alignment.CenterEnd).offset(x = out).fillMaxHeight().width(ComboDimens.dropMark)
            DropSide.Above -> Modifier.align(Alignment.TopCenter).offset(y = -out).fillMaxWidth().height(ComboDimens.dropMark)
            DropSide.Below -> Modifier.align(Alignment.BottomCenter).offset(y = out).fillMaxWidth().height(ComboDimens.dropMark)
        }
        Box(mark.background(AppTheme.colors.accentBlue))
    }
}
