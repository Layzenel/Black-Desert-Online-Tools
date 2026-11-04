package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.theme.AppTheme

/** Крестик в тёмном круге, которым с портрета убирают значок или надпись. */
@Composable
internal fun RemoveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier
            .clip(CircleShape)
            .background(colors.backgroundDeep)
            .border(AppTheme.dimens.borderWidth, colors.accentGoldPale, CircleShape)
            .padding(AppTheme.dimens.spaceXxs),
    ) {
        CloseButton(onClick)
    }
}
