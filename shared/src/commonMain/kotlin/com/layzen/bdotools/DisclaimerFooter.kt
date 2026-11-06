package com.layzen.bdotools

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.app_disclaimer
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Оговорка о неофициальности поверх низа страницы, не отнимает места у фич:
 * её требует «Руководство по созданию фан-контента» Pearl Abyss.
 */
@Composable
internal fun DisclaimerFooter(modifier: Modifier = Modifier) {
    Text(
        stringResource(Res.string.app_disclaimer),
        modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimens.spaceL, vertical = AppTheme.dimens.spaceXs),
        style = AppTheme.typography.caption,
        color = AppTheme.colors.textMuted,
        textAlign = TextAlign.Center,
    )
}
