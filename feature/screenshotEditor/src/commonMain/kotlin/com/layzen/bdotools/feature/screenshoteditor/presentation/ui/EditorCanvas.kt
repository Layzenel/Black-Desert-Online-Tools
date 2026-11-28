package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.composable.canvas.middleButtonPan
import com.layzen.bdotools.composable.canvas.wheelZoom
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Snap
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.EditPainter
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.uikit.components.onSecondaryClick
import kotlin.math.roundToInt

/** Два клика по надписи не дольше этого — двойной клик: правка её текста. */
private const val DOUBLE_CLICK_MS = 400L

/**
 * Видимая часть скриншота с правками, вписанная в область: после обрезки — только она. Инструменты рисуют мышью,
 * нарисованное сразу выбрано; клик по правке выбирает её, а тянуть — двигает (края прилипают к другим пометкам);
 * ручки выбранной правки меняют её размер, колесо над ней — толщину, Shift+колесо — размер. Колесо в остальных
 * местах меняет масштаб [viewport], зажатое колесо или пробел с левой кнопкой двигают холст. Клик инструментом
 * «Текст» открывает поле надписи на холсте, двойной клик по надписи — правит её. Правая кнопка отменяет обрезку,
 * выбирает пометку под мышью и, если [tools], открывает меню. Ctrl+Z / Ctrl+Y отменяют и повторяют, Ctrl+C / Ctrl+V /
 * Ctrl+D копируют, вставляют и дублируют пометку, Delete удаляет выбранную правку, Enter подтверждает обрезку.
 */
@Composable
internal fun EditorCanvas(
    image: ImageBitmap,
    state: ScreenshotEditorState,
    onEvent: (ScreenshotEditorEvent) -> Unit,
    measurer: TextMeasurer,
    viewport: CanvasViewport,
    modifier: Modifier = Modifier,
    tools: Boolean = true,
) {
    val painter = remember(image, measurer) { EditPainter(image, measurer) }
    var area by remember { mutableStateOf(IntSize.Zero) }
    // масштаб заводится заново после обрезки, а размер области от этого не меняется — задаём его при каждом показе
    viewport.size = Size(area.width.toFloat(), area.height.toFloat())
    val fit = ImageFit.of(IntSize(image.width, image.height), state.history.orientation, state.bounds, area)
    val pointer = CanvasPointer(state, fit, viewport, measurer)
    val currentPointer by rememberUpdatedState(pointer)
    val currentState by rememberUpdatedState(state)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val focus = remember { FocusRequester() }
    var spaceHeld by remember { mutableStateOf(false) }
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    var guides by remember { mutableStateOf(Snap()) }
    val colors = canvasColors()
    val hitTolerance = with(LocalDensity.current) { ScreenshotEditorDimens.hitTolerance.toPx() }
    // фокус на холсте сразу и после надписи: пробел, Ctrl+Z и Delete работают без клика по нему
    LaunchedEffect(state.textAt == null) { if (state.textAt == null) focus.requestFocus() }
    Box(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { area = it }
            .onFocusChanged { if (!it.hasFocus) spaceHeld = false }
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent { event ->
                when {
                    currentState.textAt != null -> event.handleTextKeys(currentOnEvent)
                    event.key == Key.Spacebar -> {
                        spaceHeld = event.type == KeyEventType.KeyDown
                        true
                    }
                    else -> event.handleEditorKeys(currentOnEvent, currentState.selected != null) {
                        currentOnEvent(ScreenshotEditorEvent.Copy(measurer))
                    }
                }
            }
            .onSecondaryClick { at ->
                // правая кнопка: как в графических редакторах — пометка под мышью выбирается, и меню про неё
                focus.requestFocus()
                currentOnEvent(ScreenshotEditorEvent.ContextPress(currentPointer.hit(at, hitTolerance)))
                if (tools) menuAt = at
            }
            .middleButtonPan(viewport, withPrimary = { spaceHeld })
            .wheelZoom(viewport),
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerHoverIcon(if (spaceHeld) PointerIcon.Hand else PointerIcon.Crosshair)
                .pointerInput(viewport) {
                    // колесо над выбранной пометкой меняет её, иначе событие уходит холсту — масштаб
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type != PointerEventType.Scroll) continue
                            val change = event.changes.first()
                            val delta = change.scrollDelta.let { if (it.y != 0f) it.y else it.x }
                            val wheel = currentPointer.wheel(change.position, delta, event.keyboardModifiers.isShiftPressed) ?: continue
                            currentOnEvent(wheel)
                            change.consume()
                        }
                    }
                }
                .pointerInput(viewport) {
                    var lastTap: Pair<Long, Int>? = null
                    // правка начинается точно там, где нажали: detectDragGestures начинает её после порога сдвига
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        // окно поверх игры тянется за любое место: правка на холсте его двигать не должна
                        down.consume()
                        focus.requestFocus()
                        val state = currentState
                        val press = currentPointer.press(down.position, ScreenshotEditorDimens.hitTolerance.toPx(), ScreenshotEditorDimens.handleReach.toPx())
                        // пометку тянут, а не рисуют: только тогда она прилипает к другим
                        val moving = press.hit.takeIf { press.resize == null && state.pendingCrop == null && state.textAt == null && !state.picking }
                        currentOnEvent(press)
                        var moved = false
                        drag(down.id) { change ->
                            moved = true
                            change.consume()
                            val to = currentPointer.toImage(change.position)
                            if (moving == null) {
                                currentOnEvent(ScreenshotEditorEvent.DragMove(to))
                            } else {
                                val (snappedTo, snap) = currentPointer.snapped(moving, press.at, to, ScreenshotEditorDimens.snapReach.toPx())
                                guides = snap
                                currentOnEvent(ScreenshotEditorEvent.DragMove(snappedTo))
                            }
                        }
                        guides = Snap()
                        currentOnEvent(ScreenshotEditorEvent.DragEnd)
                        if (moved) return@awaitEachGesture
                        currentOnEvent(ScreenshotEditorEvent.Tap(press.at))
                        val label = press.hit?.takeIf { state.history.edits.getOrNull(it) is Edit.Label }
                        val previous = lastTap
                        lastTap = label?.let { down.uptimeMillis to it }
                        if (label != null && previous != null && previous.second == label && down.uptimeMillis - previous.first <= DOUBLE_CLICK_MS) {
                            lastTap = null
                            currentOnEvent(ScreenshotEditorEvent.EditLabel(label))
                        }
                    }
                },
        ) {
            paintCanvas(CanvasScene(image, state, painter, fit, viewport, measurer, guides), colors)
        }
        CanvasControls(state, fit, viewport, measurer, onEvent)
        CanvasTextInput(state, fit, viewport, onEvent)
        menuAt?.let { at ->
            // новое меню в новой точке — заново, а закрытие старого не трогает уже открытое новое
            key(at) {
                Box(Modifier.offset { IntOffset(at.x.roundToInt(), at.y.roundToInt()) }) {
                    CanvasContextMenu(state, onDismiss = { if (menuAt == at) menuAt = null }, onEvent = onEvent)
                }
            }
        }
    }
}
