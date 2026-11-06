package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.restore

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.uikit.theme.AppTheme

/** Подпись под портретом в окне восстановления: дата копии или «Сейчас». */
@Composable
internal fun PreviewCaption(text: String) {
    Text(
        text,
        Modifier.fillMaxWidth(),
        style = AppTheme.typography.caption,
        color = AppTheme.colors.textMuted,
        textAlign = TextAlign.Center,
        maxLines = 2,
    )
}
