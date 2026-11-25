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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.uikit.components.windowDecorationHeight
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay
import java.awt.GraphicsEnvironment

/**
 * Окно меню оверлея: своё место на экране, отдельное от кнопки ([savedPanelPosition]); тянется за шапку, а пока
 * меню не двигали, открывается под кнопкой. Видно, пока меню раскрыто и игра на переднем плане. Окно размером
 * с меню. То, что вкладка показывает рядом ([aside]), — в своём окне ([AsideWindow]): когда оно было в окне меню,
 * окно росло и сдвигалось под курсором, Compose терял наведение на пункт, схема пряталась, окно сжималось — и так
 * по кругу (меню у правого края экрана дёргалось).
 */
@Composable
internal actual fun OverlayPanelWindow(open: Boolean, aside: (@Composable () -> Unit)?, content: @Composable (dragModifier: Modifier) -> Unit) {
    val gameActive by rememberGameActive()
    val shown = gameActive && open
    // левый верхний угол меню
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
        // скрытому окну Compose возвращает прежний размер, поэтому размер и место ставятся заново и при показе.
        // Перетаскивание (место меню) окно двигает само ([windowDrag]): если ставить окно и здесь, два источника
        // спорили, и меню дёргалось за курсором
        LaunchedEffect(measured, shown) {
            window.setBounds(panel.x.value.roundToInt(), panel.y.value.roundToInt(), measured.width.value.roundToInt(), measured.height.value.roundToInt())
            // состояние окна — то же, что у самого окна: иначе Compose при перерисовке вернул бы прежние размер и место
            state.size = measured
            state.position = WindowPosition(panel.x, panel.y)
        }
        val density = LocalDensity.current
        val fit = Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
            .onSizeChanged {
                // закрытое меню пустое (0×0): окно такого размера не перерисовывается и при открытии не узнало бы
                // размер меню — оставалось невидимым
                if (it.width > 0 && it.height > 0) measured = with(density) { DpSize(it.width.toDp(), it.height.toDp()) }
            }
        val drag = Modifier.windowDrag(window) { dx, dy ->
            panel = DpOffset(panel.x + dx.dp, panel.y + dy.dp)
            savePosition(PANEL_ID, panel.x, panel.y)
        }
        Box(fit) { content(drag) }
    }
    aside?.let { AsideWindow(panel, it) }
}

/**
 * Окно рядом с меню, левый верхний угол которого — [panel]: вровень с верхом меню, со стороны центра экрана (меню
 * в правой половине экрана — слева от него). Клики проходят сквозь окно. Показывается, когда замерено и встало
 * на место, — иначе мелькнуло бы в стороне.
 */
@Composable
private fun AsideWindow(panel: DpOffset, content: @Composable () -> Unit) {
    val alignEnd = isInRightHalf(panel.x + OverlayDimens.panelWidth / 2, panel.y)
    val top = panel.y + windowDecorationHeight()
    val gap = AppTheme.dimens.spaceXs
    fun left(width: Dp) = if (alignEnd) panel.x - gap - width else panel.x + OverlayDimens.panelWidth + gap
    val start = DpSize(OverlayDimens.panelWidth, OverlayDimens.panelWidth)
    val state = rememberWindowState(position = WindowPosition(left(start.width), top), size = start)
    // окно показывается, когда замерено и встало на место; размер содержимого — для перестановки, если меню сдвинули
    var placed by remember { mutableStateOf(false) }
    var measured by remember { mutableStateOf(start) }
    Window(
        onCloseRequest = {},
        state = state,
        visible = placed,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(window) { setClickThrough(window, enabled = true) }
        // схема дорастает, пока грузятся иконки и клавиши: размер и место — одним setBounds, как у окна меню (через
        // state окно отставало от содержимого, и схема обрезалась у края меню). Прямо в замере, а не в эффекте: у скрытого
        // окна эффекты не запускаются, и окно так и не показывалось
        fun place(size: DpSize) {
            val x = left(size.width)
            window.setBounds(x.value.roundToInt(), top.value.roundToInt(), size.width.value.roundToInt(), size.height.value.roundToInt())
            state.size = size
            state.position = WindowPosition(x, top)
        }
        LaunchedEffect(panel, alignEnd) { if (placed) place(measured) }
        val density = LocalDensity.current
        Box(
            Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).onSizeChanged {
                if (it.width == 0 || it.height == 0) return@onSizeChanged
                measured = with(density) { DpSize(it.width.toDp(), it.height.toDp()) }
                place(measured)
                placed = true
            },
        ) { content() }
    }
}

/** Точка ([x], [y]) — в правой половине своего экрана (несколько мониторов: того, где она стоит). */
private fun isInRightHalf(x: Dp, y: Dp): Boolean {
    val px = x.value.roundToInt()
    val py = y.value.roundToInt()
    val screens = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.map { it.defaultConfiguration.bounds }
    val screen = screens.firstOrNull { px in it.x until it.x + it.width && py in it.y until it.y + it.height } ?: screens.first()
    return px > screen.x + screen.width / 2
}
