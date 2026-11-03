package com.layzen.bdotools.uikit.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.uikit.theme.AppTheme

/** Крупное золотое значение: текущий уровень, результат расчёта, выбранное число. */
@Composable
fun ValueText(text: String, modifier: Modifier = Modifier, color: Color = AppTheme.colors.accentGold) {
    Text(text, modifier, style = AppTheme.typography.value, color = color)
}
