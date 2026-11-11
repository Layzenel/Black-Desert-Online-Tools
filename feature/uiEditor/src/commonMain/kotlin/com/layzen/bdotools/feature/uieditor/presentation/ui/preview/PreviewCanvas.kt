package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.chatTitle
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.BackgroundImage
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.CanvasViewport
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.CrosshairLayer
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.ScreenCanvas
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.frameColor
import com.layzen.bdotools.feature.uieditor.presentation.ui.handleHistoryKeys
import com.layzen.bdotools.feature.uieditor.presentation.ui.slotTitle
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Раскладка [layout] на экране игры [screen] ([ScreenCanvas]: зум колесом, сдвиг, обводка, как в редакторе), окна чата —
 * под элементами, отмечаются кликом, как и они ([includedChats]). Невыбранные
 * элементы приглушены, не влезающие на экран — с красной заливкой. Клик по рамке отмечает элемент или снимает отметку,
 * обводка отмечает задетые элементы, а если все они уже отмечены — снимает. [background] — подложка под рамками, затенённая на [backgroundDim] %.
 * Ctrl+Z и Ctrl+Shift+Z (или Ctrl+Y) отменяют и повторяют отметки.
 */
@Composable
internal fun PreviewCanvas(
    layout: AnchoredLayout,
    included: Set<SlotId>,
    includedChats: Set<Int>,
    screen: ScreenSpace,
    background: ImageBitmap?,
    backgroundDim: Int,
    onEvent: (SlotsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    // Другой монитор — другие пропорции окна, поэтому масштаб и сдвиг начинаются заново
    val viewport = remember(screen) { CanvasViewport() }
    val focus = remember { FocusRequester() }
    ScreenCanvas(
        screen = screen,
        viewport = viewport,
        focus = focus,
        onKey = { it.handleHistoryKeys(onUndo = { currentOnEvent(SlotsEvent.PreviewUndo) }, onRedo = { currentOnEvent(SlotsEvent.PreviewRedo) }) },
        onTap = {},
        onArea = { area, _ -> currentOnEvent(SlotsEvent.PreviewArea(area)) },
        modifier = modifier,
    ) { scale ->
        background?.let { BackgroundImage(it, backgroundDim) }
        CrosshairLayer(screen, scale, Modifier.fillMaxSize())
        // Вкладки главного окна отдельно не рисуются: они отмечаются в списке слева
        layout.chats.filterValues { !it.combined }.forEach { (window, chat) ->
            PreviewChatFrame(chat.placeOn(screen), chatTitle(window), screen, scale, window in includedChats) {
                focus.requestFocus()
                onEvent(SlotsEvent.PreviewInclude(emptySet(), chats = setOf(window), include = window !in includedChats))
            }
        }
        layout.positions.filterKeys { it.isActiveWith(layout.customQuickSlots) }.forEach { (id, anchored) ->
            PreviewFrame(id, anchored.placeOn(screen), screen, scale, id in included) {
                focus.requestFocus()
                onEvent(SlotsEvent.PreviewInclude(setOf(id), id !in included))
            }
        }
    }
}

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
