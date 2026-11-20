package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.PlusIcon
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Пустой слот поменьше ячейки с «+» и подсказкой [hint]: в конце комбо, в конце ветки развилки и опциональных умений. */
@Composable
internal fun AddCellSlot(hint: String, onClick: () -> Unit) {
    // по высоте ячейки, чтобы «+» стоял на одной линии с иконками умений
    Box(Modifier.height(ComboDimens.editorIcon), contentAlignment = Alignment.Center) {
        HoverTooltip(hint) {
            Box(
                Modifier.size(ComboDimens.addSlot).background(AppTheme.colors.backgroundRaised)
                    .border(ComboDimens.frame, AppTheme.colors.border)
                    .pointerHoverIcon(PointerIcon.Hand).clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) { PlusIcon() }
        }
    }
}

/** Маленькая квадратная кнопка с подсказкой [hint]. */
@Composable
internal fun SmallButton(hint: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    HoverTooltip(hint) {
        Box(
            Modifier.size(ComboDimens.gapButton).background(AppTheme.colors.backgroundRaised, AppShapes.Small)
                .border(ComboDimens.frame, AppTheme.colors.border, AppShapes.Small)
                .pointerHoverIcon(PointerIcon.Hand).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}
