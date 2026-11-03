package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Заголовок блока настроек с бежевым названием и приглушённым пояснением под ним. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    warning: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
        Text(title, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
        if (warning != null) WarningText(warning)
        if (description != null) DescriptionText(description)
    }
}

@Composable
fun DescriptionText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
}

@Composable
fun WarningText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = AppTheme.typography.caption, color = AppTheme.colors.danger)
}

/** Крупное золотое значение: текущий уровень, результат расчёта, выбранное число. */
@Composable
fun ValueText(text: String, modifier: Modifier = Modifier, color: Color = AppTheme.colors.accentGold) {
    Text(text, modifier, style = AppTheme.typography.value, color = color)
}

@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(AppTheme.dimens.borderWidth)
            .background(AppTheme.colors.divider),
    )
}

enum class LabelBadgeKind { New, Hot }

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
