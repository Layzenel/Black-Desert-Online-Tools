package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Слайдер как «Масштаб интерфейса»: подписи min/max по краям, текущее значение
 * золотом по центру, тонкая тёмная дорожка и белый ромб-ползунок.
 */
@Composable
fun ValueSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val currentOnChange by rememberUpdatedState(onValueChange)
    Column(modifier) {
        if (showLabels) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(range.first.toString(), style = AppTheme.typography.bodyBold, color = colors.textPrimary)
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ValueText(value.toString(), color = colors.accentGold)
                }
                Text(range.last.toString(), style = AppTheme.typography.bodyBold, color = colors.textPrimary)
            }
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(dimens.sliderTouchHeight)
                .pointerHoverIcon(PointerIcon.Hand),
            contentAlignment = Alignment.CenterStart,
        ) {
            val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            fun valueAt(x: Float): Int {
                val fraction = (x / widthPx).coerceIn(0f, 1f)
                return range.first + (fraction * (range.last - range.first)).roundToInt()
            }
            val fraction = (value - range.first).toFloat() / (range.last - range.first).coerceAtLeast(1)
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(range, widthPx) { detectTapGestures { currentOnChange(valueAt(it.x)) } }
                    .pointerInput(range, widthPx) {
                        detectHorizontalDragGestures { change, _ -> currentOnChange(valueAt(change.position.x)) }
                    },
            )
            Box(Modifier.fillMaxWidth().height(dimens.sliderTrackHeight).background(colors.track))
            Box(Modifier.width(maxWidth * fraction).height(dimens.sliderTrackHeight).background(colors.textMuted))
            Box(
                Modifier
                    .offset(x = maxWidth * fraction - dimens.sliderThumbSize / 2)
                    .size(dimens.sliderThumbSize)
                    .rotate(45f)
                    .background(colors.textPrimary),
            )
        }
    }
}
