package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.uikit.theme.AppTheme

/** Цвет рамки элемента группы: на холсте и в легенде одинаковый. Перезарядка — красный, хотбар — синий, классовый ресурс — жёлтый, прочие — приглушённый бежевый. */
@Composable
internal fun SlotGroup.frameColor(): Color = when (this) {
    SlotGroup.Cooldown -> AppTheme.colors.accentRed
    SlotGroup.Hotbar -> AppTheme.colors.accentBlue
    SlotGroup.ClassResource -> AppTheme.colors.badgeNew
    SlotGroup.Interface -> AppTheme.colors.textMuted
}
