package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Число в панели, которое не двигает соседей: ширина — по самому широкому значению [widest] (например, трёхзначному),
 * а [text] прижат к правому краю. Невидимый [widest] задаёт размер, поэтому панель не прыгает, когда цифры меняются.
 */
@Composable
internal fun FixedWidthText(text: String, widest: String, style: TextStyle, color: Color) {
    Box(contentAlignment = Alignment.CenterEnd) {
        Text(widest, style = style, color = Color.Transparent, softWrap = false)
        Text(text, style = style, color = color, softWrap = false)
    }
}
