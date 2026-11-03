package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Заголовок блока настроек с бежевым названием и приглушённым пояснением под ним. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    warning: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
        Text(title, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
        if (warning != null) WarningText(warning)
        if (description != null) DescriptionText(description)
    }
}
