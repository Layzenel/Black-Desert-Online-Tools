package com.layzen.bdotools.feature.comboassist.presentation.ui.skill

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Перезарядка умения в списке: значок часов и секунды («8», «1.5»), чтобы число не путали с рангом. */
@Composable
internal fun CooldownBadge(cooldownMs: Long, modifier: Modifier = Modifier) {
    val color = AppTheme.colors.textBeige
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs), verticalAlignment = Alignment.CenterVertically) {
        ClockIcon(color)
        Text(cooldownSeconds(cooldownMs), style = AppTheme.typography.bodyBold, color = color)
    }
}

/** Часы: циферблат и стрелки на 12 и 3 — знак перезарядки, понятный и в 12 пикселях. */
@Composable
private fun ClockIcon(color: Color) {
    Canvas(Modifier.size(ComboDimens.cooldownIcon)) {
        val stroke = ComboDimens.frame.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - stroke / 2
        drawCircle(color, radius, center, style = Stroke(stroke))
        drawLine(color, center, Offset(center.x, center.y - radius * HAND_LONG), stroke)
        drawLine(color, center, Offset(center.x + radius * HAND_SHORT, center.y), stroke)
    }
}

private const val HAND_LONG = 0.7f
private const val HAND_SHORT = 0.5f
