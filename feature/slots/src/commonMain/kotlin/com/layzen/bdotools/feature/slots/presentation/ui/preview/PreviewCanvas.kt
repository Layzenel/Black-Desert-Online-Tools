package com.layzen.bdotools.feature.slots.presentation.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.AnchoredLayout
import com.layzen.bdotools.feature.slots.domain.ScreenArea
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotPosition
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.presentation.ui.editor.CrosshairLayer
import com.layzen.bdotools.feature.slots.presentation.ui.editor.MarqueeFrame
import com.layzen.bdotools.feature.slots.presentation.ui.editor.frameColor
import com.layzen.bdotools.feature.slots.presentation.ui.slotTitle
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Раскладка [layout] на экране игры [screen]. Невыбранные элементы приглушены, не влезающие на экран — с красной
 * заливкой. Клик по рамке отмечает элемент или снимает отметку, Shift и протянуть — обводка рамкой.
 */
@Composable
internal fun PreviewCanvas(
    layout: AnchoredLayout,
    included: Set<SlotId>,
    screen: ScreenSpace,
    onEvent: (SlotsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    var marquee by remember { mutableStateOf<Rect?>(null) }
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
        val ratio = (screen.height / screen.width).toFloat()
        val width = if (maxHeight == Dp.Infinity) maxWidth else minOf(maxWidth, maxHeight / ratio)
        val scale = with(LocalDensity.current) { width.toPx() } / screen.width.toFloat()
        val currentScale by rememberUpdatedState(scale)
        Box(
            Modifier
                .size(width, width * ratio)
                .clipToBounds()
                .background(AppTheme.colors.track)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
                .marqueeGesture(
                    onMarquee = { marquee = it },
                    onArea = { currentOnEvent(SlotsEvent.PreviewArea(it.toScreenArea(currentScale))) },
                ),
        ) {
            CrosshairLayer(screen, scale, Modifier.fillMaxSize())
            layout.positions.forEach { (id, anchored) ->
                PreviewFrame(id, anchored.placeOn(screen), screen, scale, id in included) {
                    onEvent(SlotsEvent.PreviewInclude(setOf(id), id !in included))
                }
            }
            marquee?.let { MarqueeFrame(it) }
        }
    }
}

/** Прямоугольник в пикселях превью → UI-px экрана игры. */
private fun Rect.toScreenArea(scale: Float) =
    ScreenArea.between((left / scale).toDouble(), (top / scale).toDouble(), (right / scale).toDouble(), (bottom / scale).toDouble())

@Composable
private fun PreviewFrame(id: SlotId, position: SlotPosition, screen: ScreenSpace, scale: Float, included: Boolean, onToggle: () -> Unit) {
    val density = LocalDensity.current
    val fill = if (screen.fits(id, position)) AppTheme.colors.backgroundDeep else AppTheme.colors.danger
    val label = when (id.group) {
        SlotGroup.ClassResource -> null
        SlotGroup.Interface -> slotTitle(id)
        else -> id.number.toString()
    }
    Box(
        Modifier
            .offset { IntOffset((screen.iconLeft(id, position) * scale).roundToInt(), (screen.iconTop(id, position) * scale).roundToInt()) }
            .size(with(density) { (id.width * scale).toDp() }, with(density) { (id.height * scale).toDp() })
            .alpha(if (included) 1f else SlotsDimens.HIDDEN_ALPHA)
            .background(fill.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA))
            .border(SlotsDimens.slotBorder, id.group.frameColor())
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        if (label != null) {
            Text(
                label,
                style = AppTheme.typography.badge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
