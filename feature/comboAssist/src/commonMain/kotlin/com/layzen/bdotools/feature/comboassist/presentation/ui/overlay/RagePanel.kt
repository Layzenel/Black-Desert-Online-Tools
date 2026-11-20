package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import com.layzen.bdotools.uikit.theme.AppColors
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.PI
import kotlin.math.sin

/** Языков пламени по ширине подложки. */
private const val FLAMES = 9

/** Период колыхания пламени, мс. */
private const val FLAME_PERIOD_MS = 1800

/** Доля высоты подложки, до которой поднимается самое высокое пламя. */
private const val FLAME_HEIGHT = 0.75f

/**
 * Подложка трекера под гневом Чёрного духа вместо обычного тултипа ([TooltipPanel][com.layzen.bdotools.uikit.components.TooltipPanel]):
 * тело от тёмно-красного к чёрному, красная рамка, которая медленно разгорается и гаснет, и чёрно-красный огонь,
 * колышущийся снизу за содержимым. Отступы те же, что у тултипа: окно трекера не меняет размер при смене режима.
 */
@Composable
internal fun RagePanel(content: @Composable () -> Unit) {
    val dimens = AppTheme.dimens
    val colors = AppTheme.colors
    val phase by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(FLAME_PERIOD_MS, easing = LinearEasing), RepeatMode.Restart),
    )
    val glow = (sin(phase * 2 * PI).toFloat() + 1) / 2
    Box(
        Modifier
            .shadow(dimens.menuShadow)
            .background(Brush.verticalGradient(listOf(colors.rageDark, colors.rageDeep)))
            .drawBehind { drawFlames(phase, colors) }
            .border(dimens.borderWidth, lerp(colors.rageBorder, colors.rageEmber, glow))
            .padding(dimens.tooltipFrameGap)
            .border(dimens.borderWidth, colors.rageBorder)
            .padding(horizontal = dimens.spaceS, vertical = dimens.spaceXs),
    ) { content() }
}

/** Языки пламени вдоль нижнего края: у каждого своя фаза и скорость, основание — угли [AppColors.rageEmber]. */
private fun DrawScope.drawFlames(phase: Float, colors: AppColors) {
    val step = size.width / FLAMES
    for (i in 0 until FLAMES) {
        val wave = sin((phase * (1 + i % 3) + i * GOLDEN) * 2 * PI).toFloat()
        val height = size.height * FLAME_HEIGHT * (BASE_SHARE + SWING_SHARE * (wave + 1) / 2)
        val sway = step * SWAY_SHARE * wave
        val left = i * step - step * OVERLAP
        val right = (i + 1) * step + step * OVERLAP
        val tip = (left + right) / 2 + sway
        val top = size.height - height
        val flame = Path().apply {
            moveTo(left, size.height)
            quadraticTo(left + (tip - left) * CURVE, size.height - height * CURVE, tip, top)
            quadraticTo(right - (right - tip) * CURVE, size.height - height * CURVE, right, size.height)
            close()
        }
        drawPath(
            flame,
            Brush.verticalGradient(
                listOf(Color.Transparent, colors.rageFlame.copy(alpha = FLAME_ALPHA), colors.rageEmber.copy(alpha = EMBER_ALPHA)),
                startY = top, endY = size.height,
            ),
        )
    }
}

private const val GOLDEN = 0.618f
private const val BASE_SHARE = 0.35f
private const val SWING_SHARE = 0.65f
private const val SWAY_SHARE = 0.3f
private const val OVERLAP = 0.4f
private const val CURVE = 0.55f
private const val FLAME_ALPHA = 0.35f
private const val EMBER_ALPHA = 0.55f
