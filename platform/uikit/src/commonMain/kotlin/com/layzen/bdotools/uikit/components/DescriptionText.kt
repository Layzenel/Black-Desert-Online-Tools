package com.layzen.bdotools.uikit.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Приглушённое пояснение под заголовком или полем. */
@Composable
fun DescriptionText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
}
