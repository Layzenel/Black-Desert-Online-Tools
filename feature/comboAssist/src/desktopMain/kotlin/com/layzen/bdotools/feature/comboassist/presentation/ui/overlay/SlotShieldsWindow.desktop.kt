package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.composable.overlay.rememberGameActive
import com.layzen.bdotools.composable.overlay.setClickThrough
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.uikit.theme.AppTheme
import java.awt.Toolkit

/** Порядок щитов от правого края слота влево: самая сильная защита — с края. */
private val SHIELD_ORDER = listOf(SkillProtection.Invincible, SkillProtection.ForwardGuard, SkillProtection.SuperArmor)

/** Ширина щита — доля стороны слота: щиты растут вместе с «Размером интерфейса» игры. */
private const val SHIELD_SHARE = 0.3f

/** Отступ иконки от края слота (его рамка) — доля стороны слота: щиты стоят в углу самой иконки. */
private const val ICON_INSET_SHARE = 0.08f

/** Шаг щитов в ряду — доля ширины: соседние чуть заходят друг на друга. */
private const val STEP_SHARE = 0.85f

/**
 * Окно щитов: прозрачное, без рамки и фокуса, поверх всех окон, клики сквозь него уходят в игру
 * ([setClickThrough]). Накрывает только слоты с защитой, щиты — внутри иконки; координаты слотов — доли экрана, окно —
 * в логических пикселях Java, так масштаб Windows не мешает. Видно, только пока игра на переднем плане.
 */
@Composable
internal actual fun SlotShieldsWindow(slots: List<ScreenSlot>, protections: Map<Int, Set<SkillProtection>>) {
    val screen = remember { Toolkit.getDefaultToolkit().screenSize }
    val marked = slots.mapNotNull { slot ->
        val kinds = SHIELD_ORDER.filter { it in protections[slot.skillNo].orEmpty() }
        if (kinds.isEmpty()) return@mapNotNull null
        val side = (slot.size * screen.height).toFloat()
        val left = (slot.centerX * screen.width).toFloat() - side / 2
        val top = (slot.centerY * screen.height).toFloat() - side / 2
        Rect(Offset(left, top), Size(side, side)) to kinds
    }
    if (marked.isEmpty()) return
    val bounds = marked.map { it.first }.reduce { a, b -> Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom)) }
    val gameActive by rememberGameActive()
    val position = WindowPosition(bounds.left.dp, bounds.top.dp)
    val size = DpSize(bounds.width.dp, bounds.height.dp)
    val state = rememberWindowState(position = position, size = size)
    LaunchedEffect(bounds) {
        state.position = position
        state.size = size
    }
    val colors = SHIELD_ORDER.associateWith { shieldColor(it) }
    val outline = AppTheme.colors.track
    Window(
        onCloseRequest = {},
        state = state,
        visible = gameActive,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(window) { setClickThrough(window, enabled = true) }
        Canvas(Modifier.fillMaxSize()) {
            marked.forEach { (rect, kinds) ->
                val width = (rect.width * SHIELD_SHARE).dp.toPx()
                val inset = (rect.width * ICON_INSET_SHARE).dp.toPx()
                val right = (rect.right - bounds.left).dp.toPx() - inset
                val top = (rect.top - bounds.top).dp.toPx() + inset
                // самый сильный щит у края рисуется последним, поверх соседей
                kinds.indices.reversed().forEach { index ->
                    val left = right - width - index * width * STEP_SHARE
                    drawShield(Offset(left, top), width, colors.getValue(kinds[index]), outline)
                }
            }
        }
    }
}
