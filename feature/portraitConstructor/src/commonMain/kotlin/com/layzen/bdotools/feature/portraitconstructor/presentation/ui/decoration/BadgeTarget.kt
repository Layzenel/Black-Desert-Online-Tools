package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

private const val EMPTY_FILL_ALPHA = 0.6f
private const val PLUS_RATIO = 0.34f

/**
 * Круглое место значка на предпросмотре. Пустое — тёмный круг с «+», нарисованным линиями точно по центру;
 * занятое — прозрачное, потому что сам значок уже нарисован на портрете. При наведении кольцо золотое.
 */
@Composable
internal fun BadgeTarget(filled: Boolean, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val line = PortraitsDimens.badgeLineWidth
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Canvas(
        modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val stroke = line.toPx()
        val radius = size.minDimension / 2 - stroke / 2
        val ring = if (hovered) colors.accentGold else colors.textMuted
        if (!filled) {
            drawCircle(colors.backgroundDeep.copy(alpha = EMPTY_FILL_ALPHA), radius)
            val arm = size.minDimension * PLUS_RATIO / 2
            drawLine(ring, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), stroke, StrokeCap.Round)
            drawLine(ring, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), stroke, StrokeCap.Round)
        }
        if (!filled || hovered) drawCircle(ring, radius, style = Stroke(stroke))
    }
}
