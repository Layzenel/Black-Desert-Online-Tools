package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Box
import com.layzen.bdotools.composable.overlay.rememberGameActive
import com.layzen.bdotools.composable.overlay.setClickThrough
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillTooltipCard
import com.layzen.bdotools.uikit.components.TooltipPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.awt.Toolkit

/** Как часто смотреть, где курсор. */
private const val POLL_MS = 100L

/** Сколько курсор должен простоять над слотом, чтобы показался тултип. */
private const val HOVER_MS = 300L

/** Слоты левее этой доли экрана получают тултип слева, остальные — справа: так он не закрывает соседние слоты. */
private const val LEFT_SIDE_SHARE = 0.45

/**
 * Окно тултипа слота: прозрачное, без рамки и фокуса, поверх всех окон, клики сквозь него уходят в игру
 * ([setClickThrough]). Стоит сбоку от слота и не выходит за низ экрана. Курсор проверяется, только пока игра
 * на переднем плане.
 */
@Composable
internal actual fun SlotTooltipWindow(
    slots: List<ScreenSlot>,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    protections: Map<Int, Set<SkillProtection>>,
    controls: Map<Int, Set<CrowdControl>>,
) {
    if (slots.isEmpty()) return
    val gameActive by rememberGameActive()
    val screen = remember { Toolkit.getDefaultToolkit().screenSize }
    val marks = slots.mapNotNull { SlotMarks.of(it, protections[it.skillNo].orEmpty(), controls[it.skillNo].orEmpty(), screen) }
    val hovered by produceState<ScreenSlot?>(null, slots, marks, gameActive) {
        value = null
        if (!gameActive) return@produceState
        var candidate: ScreenSlot? = null
        var since = 0L
        while (true) {
            val slot = withContext(Dispatchers.IO) { slotUnderCursor(slots, marks) }
            val now = System.currentTimeMillis()
            if (slot != candidate) {
                candidate = slot
                since = now
            }
            value = candidate?.takeIf { now - since >= HOVER_MS }
            delay(POLL_MS)
        }
    }
    val slot = hovered ?: return
    val item = skills[slot.skillNo] ?: return
    val start = DpSize(ComboDimens.slotTooltipWidth, ComboDimens.slotTooltipWidth)
    val state = rememberWindowState(position = besideSlot(slot, start), size = start)
    // окно показывается, только когда размер замерен и оно встало у слота: иначе мелькнуло бы в углу экрана
    var placed by remember(slot) { mutableStateOf(false) }
    LaunchedEffect(slot, state.size) { state.position = besideSlot(slot, state.size) }
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
                    state.position = besideSlot(slot, state.size)
                    placed = true
                },
        ) { TooltipPanel { SkillTooltipCard(item, loadDetails, null) } }
    }
}

/** Угол тултипа: сбоку от слота [slot] на уровне его верха, сдвинут вверх, если не помещается по высоте. */
private fun besideSlot(slot: ScreenSlot, size: DpSize): WindowPosition {
    val screen = Toolkit.getDefaultToolkit().screenSize
    val side = (slot.size * screen.height).dp
    val centerX = (slot.centerX * screen.width).dp
    val top = (slot.centerY * screen.height).dp - side / 2
    val gap = ComboDimens.slotTooltipGap
    val x = if (slot.centerX < LEFT_SIDE_SHARE) centerX - side / 2 - gap - size.width else centerX + side / 2 + gap
    val y = minOf(top, screen.height.dp - size.height).coerceAtLeast(0.dp)
    return WindowPosition(x, y)
}
