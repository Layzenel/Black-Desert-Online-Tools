package com.layzen.bdotools.feature.portraits.presentation.ui.notice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_dismiss
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NoticeBar(notice: Notice, onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        val text = noticeText(notice)
        if (notice.isError) {
            WarningText(text, Modifier.weight(1f))
        } else {
            Text(text, Modifier.weight(1f), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
        }
        ActionButton(stringResource(Res.string.portraits_dismiss), onClick = onDismiss)
    }
}
