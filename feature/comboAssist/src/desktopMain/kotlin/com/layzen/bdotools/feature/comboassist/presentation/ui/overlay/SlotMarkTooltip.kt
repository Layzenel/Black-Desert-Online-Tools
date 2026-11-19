package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.composable.overlay.isCursorShown
import com.layzen.bdotools.composable.overlay.setClickThrough
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import java.awt.MouseInfo
import java.awt.Point

/** Как часто смотреть, где курсор. */
private const val POLL_MS = 100L

/**
 * Подсказка с названием защиты или контроля, когда курсор над щитом или значком контроля на слоте ([marks]).
 * Своё прозрачное окно у курсора, клики сквозь него уходят в игру. Курсор, спрятанный игрой, ни над чем не стоит.
 * [active] — окно щитов сейчас видно.
 */
@Composable
internal fun SlotMarkTooltip(marks: List<SlotMarks>, active: Boolean) {
    val hovered by produceState<Pair<StringResource, Point>?>(null, marks, active) {
        value = null
        if (!active) return@produceState
        while (true) {
            value = withContext(Dispatchers.IO) { markUnderCursor(marks) }
            delay(POLL_MS)
        }
    }
    val (title, pointer) = hovered ?: return
    val offset = ComboDimens.markTooltipOffset
    val at = WindowPosition(pointer.x.dp + offset, pointer.y.dp + offset)
    val state = rememberWindowState(position = at, size = DpSize(ComboDimens.slotTooltipWidth, ComboDimens.slotTooltipWidth))
    // окно показывается, только когда размер замерен: иначе мелькнуло бы большим пустым прямоугольником
    var placed by remember(title) { mutableStateOf(false) }
    LaunchedEffect(at) { state.position = at }
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
        val density = LocalDensity.current
        Box(
            Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
                .onSizeChanged {
                    state.size = with(density) { DpSize(it.width.toDp(), it.height.toDp()) }
                    placed = true
                },
        ) {
            TooltipPanel { Text(stringResource(title), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary) }
        }
    }
}

/** Название значка под курсором и сам курсор, или `null`. */
private fun markUnderCursor(marks: List<SlotMarks>): Pair<StringResource, Point>? {
    if (!isCursorShown()) return null
    val pointer = MouseInfo.getPointerInfo()?.location ?: return null
    val point = Offset(pointer.x.toFloat(), pointer.y.toFloat())
    return marks.firstNotNullOfOrNull { it.titleAt(point) }?.let { it to pointer }
}
