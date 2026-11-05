package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.uikit.components.onSecondaryClick
import kotlin.math.roundToInt

/**
 * Редактор слотов на экране игры ([ScreenCanvas]): подложка, сетка от прицела, прицел и элементы интерфейса.
 * Клик по пустому месту и Esc снимают выбор, обводка выбирает элементы, стрелки их двигают, ПКМ открывает меню.
 */
@Composable
internal fun SlotsCanvas(state: SlotsState, viewport: CanvasViewport, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val screen = state.loaded?.game?.screen ?: return
    val focus = remember { FocusRequester() }
    val currentOnEvent by rememberUpdatedState(onEvent)
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    // Фокус сразу на холсте: Ctrl+Z и стрелки работают без клика по нему
    LaunchedEffect(focus) { focus.requestFocus() }
    ScreenCanvas(
        screen = screen,
        viewport = viewport,
        focus = focus,
        onKey = { it.handleKeys(currentOnEvent) },
        onTap = { currentOnEvent(SlotsEvent.ClearSelection) },
        onArea = { area, additive -> currentOnEvent(SlotsEvent.SelectArea(area, additive)) },
        modifier = modifier,
        windowModifier = Modifier.onSecondaryClick { menuAt = it },
        overlay = {
            // Пустой Box в точке клика: меню раскрывается от его угла
            menuAt?.let { at ->
                Box(Modifier.offset { IntOffset(at.x.roundToInt(), at.y.roundToInt()) }) {
                    CanvasContextMenu(state, onDismiss = { menuAt = null }, onEvent = onEvent)
                }
            }
        },
    ) { scale ->
        CanvasContent(state, screen, scale, onEvent, onPress = { focus.requestFocus() })
    }
}

@Composable
private fun CanvasContent(state: SlotsState, screen: ScreenSpace, scale: Float, onEvent: (SlotsEvent) -> Unit, onPress: () -> Unit) {
    state.background?.takeIf { state.backgroundShown }?.let {
        Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
    GridLayer(state.grid, screen, scale, Modifier.fillMaxSize())
    CrosshairLayer(screen, scale, Modifier.fillMaxSize())
    state.slots.forEach { id ->
        val position = state.layout[id] ?: return@forEach
        SlotFrame(
            id = id,
            position = position,
            screen = screen,
            scale = scale,
            selected = id in state.selection,
            visible = state.layout.isVisible(id),
            onEvent = onEvent,
            onPress = onPress,
        )
    }
    SnapGuidesLayer(state.snapGuides, screen, scale, Modifier.fillMaxSize())
}
