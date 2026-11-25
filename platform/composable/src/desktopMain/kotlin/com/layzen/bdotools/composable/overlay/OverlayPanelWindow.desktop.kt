package com.layzen.bdotools.composable.overlay

import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay

/**
 * Окно меню оверлея: своё место на экране, отдельное от кнопки ([savedPanelPosition]); тянется за шапку, а пока
 * меню не двигали, открывается под кнопкой. Видно, пока меню раскрыто и игра на переднем плане. Окно размером
 * с содержимое: то, что вкладка показывает рядом с меню, растит окно в сторону центра экрана, а само меню стоит
 * на месте.
 */
@Composable
internal actual fun OverlayPanelWindow(open: Boolean, content: @Composable (dragModifier: Modifier, alignEnd: Boolean) -> Unit) {
    val gameActive by rememberGameActive()
    val shown = gameActive && open
    // место самого меню (левый верхний угол), а не окна: рядом с меню может стоять ещё и содержимое вкладки
    var panel by remember { mutableStateOf(savedPanelPosition()) }
    // пока меню не двигали, оно открывается под кнопкой, где бы она ни стояла сейчас
    LaunchedEffect(open) { if (open) panel = savedPanelPosition() }
    var measured by remember { mutableStateOf(DpSize(OverlayDimens.panelWidth, OverlayDimens.menuButton)) }
    val state = rememberWindowState(position = WindowPosition(panel.x, panel.y), size = measured)
    // скрытое окно не держим: у каждого окна своя поверхность отрисовки в памяти; место и размер — в state выше
    if (!shown) return
    Window(
        onCloseRequest = {},
        state = state,
        visible = shown,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        val cursorShown by rememberCursorShown()
        LaunchedEffect(window, cursorShown) { setClickThrough(window, enabled = !cursorShown) }
        LaunchedEffect(shown, OverlayLayers.overlayShown) {
            delay(RAISE_DELAY_MS)
            if (shown) window.toFront()
        }
        val screen = window.graphicsConfiguration.bounds
        val alignEnd = (panel.x + OverlayDimens.panelWidth / 2).value > screen.x + screen.width / 2
        // скрытому окну Compose возвращает прежний размер, поэтому размер и место ставятся заново и при показе.
        // Размер и место — одним setBounds: порознь окно сначала росло вправо, потом уезжало влево, и меню у правого
        // края прыгало вместе со схемой рядом. Перетаскивание (место меню) окно двигает само ([windowDrag]): если ставить
        // окно и здесь, два источника спорили, и меню дёргалось за курсором
        LaunchedEffect(measured, alignEnd, shown) {
            val left = if (alignEnd) panel.x + OverlayDimens.panelWidth - measured.width else panel.x
            window.setBounds(left.value.roundToInt(), panel.y.value.roundToInt(), measured.width.value.roundToInt(), measured.height.value.roundToInt())
            // состояние окна — то же, что у самого окна: иначе Compose при перерисовке вернул бы прежние размер и место
            state.size = measured
            state.position = WindowPosition(left, panel.y)
        }
        val density = LocalDensity.current
        // меню прижато к своему краю окна: пока окно ещё старого размера, новое содержимое рядом уходит за край, а меню
        // остаётся на месте
        val fit = Modifier.wrapContentSize(if (alignEnd) Alignment.TopEnd else Alignment.TopStart, unbounded = true)
            .onSizeChanged {
                // закрытое меню пустое (0×0): окно такого размера не перерисовывается и при открытии не узнало бы
                // размер меню — оставалось невидимым
                if (it.width > 0 && it.height > 0) measured = with(density) { DpSize(it.width.toDp(), it.height.toDp()) }
            }
        val drag = Modifier.windowDrag(window) { dx, dy ->
            panel = DpOffset(panel.x + dx.dp, panel.y + dy.dp)
            savePosition(PANEL_ID, panel.x, panel.y)
        }
        Box(fit) { content(drag, alignEnd) }
    }
}
