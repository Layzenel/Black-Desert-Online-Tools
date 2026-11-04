package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.uikit.theme.AppTheme

/** Цвет рамки слота группы: на холсте и в легенде одинаковый. */
@Composable
internal fun SlotGroup.frameColor(): Color = when (this) {
    SlotGroup.Cooldown -> AppTheme.colors.accentGoldPale
    SlotGroup.Hotbar -> AppTheme.colors.textButton
}
