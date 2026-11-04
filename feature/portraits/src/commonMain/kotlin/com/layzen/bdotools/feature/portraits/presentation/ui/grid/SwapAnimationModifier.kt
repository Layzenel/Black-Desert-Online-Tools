package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.layzen.bdotools.feature.portraits.presentation.state.SwapAnimation

private const val HIDDEN_SCALE = 0.7f
private const val OVERSHOOT_SCALE = 1.08f
private const val OVERSHOOT_AT = 0.6f

/**
 * Обмен портретов: в фазе `Out` плитка гаснет, сжимаясь, в фазе `In` проявляется с лёгким «прыжком»
 * выше своего размера. [phase] `null` — плитка в покое.
 */
@Composable
internal fun Modifier.swapAnimation(phase: SwapAnimation.Phase?): Modifier {
    val hidden = phase == SwapAnimation.Phase.Out
    val alpha by animateFloatAsState(
        targetValue = if (hidden) 0f else 1f,
        animationSpec = when (phase) {
            SwapAnimation.Phase.Out -> tween(SwapAnimation.OUT_MS, easing = FastOutLinearInEasing)
            SwapAnimation.Phase.In -> tween((SwapAnimation.IN_MS * OVERSHOOT_AT).toInt(), easing = LinearOutSlowInEasing)
            null -> snap()
        },
    )
    val scale by animateFloatAsState(
        targetValue = if (hidden) HIDDEN_SCALE else 1f,
        animationSpec = when (phase) {
            SwapAnimation.Phase.Out -> tween(SwapAnimation.OUT_MS, easing = FastOutLinearInEasing)
            SwapAnimation.Phase.In -> keyframes {
                durationMillis = SwapAnimation.IN_MS
                HIDDEN_SCALE at 0
                OVERSHOOT_SCALE at (SwapAnimation.IN_MS * OVERSHOOT_AT).toInt() using LinearOutSlowInEasing
            }
            null -> snap()
        },
    )
    return graphicsLayer {
        this.alpha = alpha
        scaleX = scale
        scaleY = scale
    }
}
