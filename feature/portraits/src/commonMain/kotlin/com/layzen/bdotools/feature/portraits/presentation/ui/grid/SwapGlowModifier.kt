package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.layzen.bdotools.feature.portraits.presentation.state.SwapAnimation
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.random.Random
import kotlinx.coroutines.launch

private const val SPARK_COUNT = 22
private const val TINT_ALPHA = 0.38f
/** Доли длительности: вспышка набирает силу до [RISE_END] и гаснет после [FADE_START]. */
private const val RISE_END = 0.15f
private const val FADE_START = 0.75f
private const val GLOW_LAYERS = 5
private const val SPARK_HALO_SCALE = 2.5f
private const val SPARK_HALO_ALPHA = 0.45f

/** Искра: место в долях плитки, снос вбок в долях подъёма, задержка и время жизни в долях анимации. */
private class Spark(val x: Float, val y: Float, val drift: Float, val delay: Float, val life: Float)

/**
 * Обмен портретов, как в окне выбора персонажа игры: плитку заливает тёплым золотом, по краю загорается
 * светящаяся рамка и вверх летят искры, а на середине вспышки картинка меняется. [phase] `null` — плитка в покое.
 */
@Composable
internal fun Modifier.swapGlow(phase: SwapAnimation.Phase?): Modifier {
    val active = phase != null
    val progress = remember { Animatable(1f) }
    val sparks = remember { List(SPARK_COUNT) { randomSpark() } }
    val scope = rememberCoroutineScope()
    LaunchedEffect(active) {
        // вспышка доигрывает сама: если кадры отстали от таймера контроллера, её нельзя оборвать на середине,
        // иначе свечение так и останется на плитке
        if (active) scope.launch {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(SwapAnimation.TOTAL_MS, easing = LinearEasing))
        }
    }
    val colors = AppTheme.colors
    return drawWithContent {
        drawContent()
        val t = progress.value
        val strength = envelope(t)
        if (strength <= 0f) return@drawWithContent
        drawRect(colors.accentGoldPale.copy(alpha = TINT_ALPHA * strength), blendMode = BlendMode.Screen)
        drawInnerGlow(colors.accentGold, strength)
        sparks.forEach { drawSpark(it, t, colors.accentGold, colors.textPrimary) }
    }
}

private fun randomSpark() = Spark(
    x = Random.nextFloat(),
    y = Random.nextFloat(),
    drift = Random.nextFloat() - 0.5f,
    delay = Random.nextFloat() * 0.35f,
    life = 0.4f + Random.nextFloat() * 0.3f,
)

/** Сила вспышки: быстро нарастает, держится и гаснет к концу. */
private fun envelope(t: Float): Float = when {
    t < RISE_END -> t / RISE_END
    t > FADE_START -> ((1f - t) / (1f - FADE_START)).coerceAtLeast(0f)
    else -> 1f
}

/** Яркая рамка по краю и свечение, которое слабеет к центру плитки. */
private fun DrawScope.drawInnerGlow(color: Color, strength: Float) {
    val edge = PortraitsDimens.swapGlowEdgeWidth.toPx()
    val depth = PortraitsDimens.swapGlowDepth.toPx()
    val step = depth / GLOW_LAYERS
    repeat(GLOW_LAYERS) { layer ->
        val inset = edge + step * (layer + 0.5f)
        val alpha = strength * (1f - (layer + 1f) / (GLOW_LAYERS + 1)) * 0.6f
        drawRect(color.copy(alpha = alpha), Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2), style = Stroke(step))
    }
    drawRect(color.copy(alpha = strength), Offset(edge / 2, edge / 2), Size(size.width - edge, size.height - edge), style = Stroke(edge))
}

/** Искра поднимается и гаснет; до своей задержки и после конца жизни не видна. */
private fun DrawScope.drawSpark(
    spark: Spark,
    t: Float,
    halo: Color,
    core: Color,
) {
    val age = (t - spark.delay) / spark.life
    if (age !in 0f..1f) return
    val rise = PortraitsDimens.swapSparkRise.toPx() * age
    val center = Offset(spark.x * size.width + spark.drift * rise, spark.y * size.height - rise)
    val alpha = if (age < 0.25f) age / 0.25f else 1f - (age - 0.25f) / 0.75f
    val radius = PortraitsDimens.swapSparkRadius.toPx() * (0.4f + 0.6f * age)
    drawCircle(halo.copy(alpha = alpha * SPARK_HALO_ALPHA), radius * SPARK_HALO_SCALE, center)
    drawCircle(core.copy(alpha = alpha), radius, center)
}
