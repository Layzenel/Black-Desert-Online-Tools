package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Плашка-пилюля «NEW» (янтарная) или «HOT» (красно-оранжевая). */
@Composable
fun LabelBadge(
    text: String,
    modifier: Modifier = Modifier,
    kind: LabelBadgeKind = LabelBadgeKind.New,
) {
    val colors = AppTheme.colors
    Text(
        text = text,
        style = AppTheme.typography.badge,
        color = colors.onBadge,
        modifier = modifier
            .background(if (kind == LabelBadgeKind.New) colors.badgeNew else colors.badgeHot, AppShapes.Pill)
            .padding(horizontal = AppTheme.dimens.spaceS, vertical = AppTheme.dimens.spaceXxs),
    )
}
