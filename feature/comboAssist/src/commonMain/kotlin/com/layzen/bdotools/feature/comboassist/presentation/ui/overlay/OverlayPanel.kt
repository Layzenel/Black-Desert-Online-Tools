package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.uikit.components.TooltipPanel

/** Подложка окон трекера: обычный тултип или, пока трекер ведёт комбо под гневом Чёрного духа ([rage]), [RagePanel]. */
@Composable
internal fun OverlayPanel(rage: Boolean, content: @Composable () -> Unit) {
    if (rage) RagePanel(content) else TooltipPanel(content)
}
