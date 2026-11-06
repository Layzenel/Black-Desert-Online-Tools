package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.theme.AppTheme

/** Строка списка превью: галочка и подпись; [bold] — заголовок группы. */
@Composable
internal fun PreviewCheckRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        CheckBox(checked, onCheckedChange)
        Text(
            text,
            style = if (bold) AppTheme.typography.bodyBold else AppTheme.typography.caption,
            color = if (bold) AppTheme.colors.textBeige else AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
