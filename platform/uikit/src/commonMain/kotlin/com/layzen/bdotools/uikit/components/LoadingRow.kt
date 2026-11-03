package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Строка «гоблин + текст» для небольших ожиданий внутри окна. */
@Composable
fun LoadingRow(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        RunningGoblin(size = AppTheme.dimens.loaderSmallSize)
        DescriptionText(text)
    }
}
