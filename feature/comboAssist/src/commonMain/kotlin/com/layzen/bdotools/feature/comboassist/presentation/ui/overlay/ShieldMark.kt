package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Ширина рисунка щита в его единицах (рисунок 13 × 15). */
private const val SHIELD_UNITS = 13f

/** Цвет щита защиты: жёлтый — устойчивость, синий — защита спереди, красный — неуязвимость. */
@Composable
internal fun shieldColor(protection: SkillProtection): Color = when (protection) {
    SkillProtection.SuperArmor -> AppTheme.colors.accentGold
    SkillProtection.ForwardGuard -> AppTheme.colors.accentBlue
    SkillProtection.Invincible -> AppTheme.colors.accentRed
}

/** Щит защиты [protection] размером [ComboDimens.shieldIcon] — тот же, что на слотах перезарядки в игре. */
@Composable
internal fun ShieldIcon(protection: SkillProtection, modifier: Modifier = Modifier) {
    val fill = shieldColor(protection)
    val outline = AppTheme.colors.track
    Canvas(modifier.size(ComboDimens.shieldIcon)) { drawShield(Offset.Zero, size.width, fill, outline) }
}

/** Щит шириной [width] с левым верхним углом в [topLeft]: заливка [fill] и тёмный контур [outline], чтобы был виден на любой иконке. */
internal fun DrawScope.drawShield(topLeft: Offset, width: Float, fill: Color, outline: Color) {
    val unit = width / SHIELD_UNITS
    fun p(x: Float, y: Float) = Offset(topLeft.x + x * unit, topLeft.y + y * unit)
    val shield = Path().apply {
        moveTo(p(6.5f, 0.8f).x, p(6.5f, 0.8f).y)
        lineTo(p(12f, 2.8f).x, p(12f, 2.8f).y)
        lineTo(p(12f, 7.2f).x, p(12f, 7.2f).y)
        cubicTo(p(12f, 10.6f).x, p(12f, 10.6f).y, p(9.6f, 12.9f).x, p(9.6f, 12.9f).y, p(6.5f, 14.2f).x, p(6.5f, 14.2f).y)
        cubicTo(p(3.4f, 12.9f).x, p(3.4f, 12.9f).y, p(1f, 10.6f).x, p(1f, 10.6f).y, p(1f, 7.2f).x, p(1f, 7.2f).y)
        lineTo(p(1f, 2.8f).x, p(1f, 2.8f).y)
        close()
    }
    drawPath(shield, fill)
    drawPath(shield, outline, style = Stroke(ComboDimens.slotShieldOutline.toPx()))
}
