package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Список пунктов [points] с золотым тире: что умеет тулза, ограничения, советы. */
@Composable
fun BulletList(points: List<String>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        points.forEach { point ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                Text(BULLET, style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
                Text(point, style = AppTheme.typography.body, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

/** Маркер пункта: тире есть в шрифте web-сборки, в отличие от «•» других шрифтов. */
private const val BULLET = "—"
