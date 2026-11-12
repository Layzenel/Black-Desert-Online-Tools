package com.layzen.bdotools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.app_disclaimer
import com.layzen.bdotools.resources.app_disclaimer_short
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Оговорка о неофициальности поверх низа страницы, одной строкой, чтобы не отнимать места у фич:
 * её требует «Руководство по созданию фан-контента» Pearl Abyss. Полный текст — в подсказке при наведении.
 */
@Composable
internal fun DisclaimerFooter(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        HoverTooltip(stringResource(Res.string.app_disclaimer)) {
            Text(
                stringResource(Res.string.app_disclaimer_short),
                Modifier.padding(horizontal = AppTheme.dimens.spaceL, vertical = AppTheme.dimens.spaceXxs),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
