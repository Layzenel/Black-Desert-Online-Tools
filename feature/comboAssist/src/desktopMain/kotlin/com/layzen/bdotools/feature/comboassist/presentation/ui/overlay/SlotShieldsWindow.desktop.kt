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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.composable.overlay.rememberCursorShown
import com.layzen.bdotools.composable.overlay.rememberOverlayShown
import com.layzen.bdotools.composable.overlay.setClickThrough
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import java.awt.Toolkit

/** Толщина рамки значка контроля — доля диаметра. */
private const val CONTROL_RING_SHARE = 0.07f

/**
 * Окно щитов: прозрачное, без рамки и фокуса, поверх всех окон, клики сквозь него уходят в игру
 * ([setClickThrough]). Накрывает только слоты с защитой или контролями ([SlotMarks]): щиты — в правом верхнем углу
 * иконки, значки контролей — круглые, в бежевой рамке, по центру нижней кромки слота. Окно — в логических пикселях
 * Java, так масштаб Windows не мешает. Видно, только пока игра на переднем плане и курсор спрятан (режим боя):
 * с курсором открыты окна игры, а закрыты ли ими слоты, по экрану надёжно не понять (2026-11-26: и по иконке,
 * и по рамке слота значки то пропадали при движении, то оставались под инвентарём).
 */
@Composable
internal actual fun SlotShieldsWindow(
    slots: List<ScreenSlot>,
    protections: Map<Int, Set<SkillProtection>>,
    controls: Map<Int, Set<CrowdControl>>,
) {
    val screen = remember { Toolkit.getDefaultToolkit().screenSize }
    val marked = slots.mapNotNull { SlotMarks.of(it, protections[it.skillNo].orEmpty(), controls[it.skillNo].orEmpty(), screen) }
    if (marked.isEmpty()) return
    val bounds = marked.flatMap { listOfNotNull(it.rect, it.controlsArea()) }.reduce { a, b -> Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom)) }
    val overlayShown by rememberOverlayShown()
    // курсор показан — открыто окно игры (инвентарь, карта, умения), и оно может закрывать слоты
    val cursorShown by rememberCursorShown()
    val position = WindowPosition(bounds.left.dp, bounds.top.dp)
    val size = DpSize(bounds.width.dp, bounds.height.dp)
    val state = rememberWindowState(position = position, size = size)
    LaunchedEffect(bounds) {
        state.position = position
        state.size = size
    }
    val colors = SkillProtection.entries.associateWith { shieldColor(it) }
    val look = MarkLook(AppTheme.colors.track, AppTheme.colors.backgroundDeep, AppTheme.colors.accentGoldPale)
    val controlIcons = CrowdControl.entries.associateWith { painterResource(it.icon) }
    Window(
        onCloseRequest = {},
        state = state,
        visible = overlayShown && !cursorShown,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(window) { setClickThrough(window, enabled = true) }
        Canvas(Modifier.fillMaxSize()) {
            // экранная точка → точка окна
            fun local(rect: Rect) = Rect(Offset((rect.left - bounds.left).dp.toPx(), (rect.top - bounds.top).dp.toPx()), Size(rect.width.dp.toPx(), rect.height.dp.toPx()))
            marked.forEach { marks ->
                // самый сильный щит у края рисуется последним, поверх соседей
                marks.shieldRects().reversed().forEach { (kind, box) -> local(box).let { drawShield(it.topLeft, it.width, colors.getValue(kind), look.outline) } }
                marks.controlRects().forEach { (control, box) -> drawControl(local(box), controlIcons.getValue(control), look) }
            }
        }
    }
}

/** Цвета значков: обводка щита, фон и рамка значка контроля. */
private class MarkLook(val outline: Color, val badgeFill: Color, val badgeRing: Color)

/** Круглый значок контроля в квадрате [box]: тёмный круг, иконка, обрезанная по кругу, и бежевая рамка. */
private fun DrawScope.drawControl(box: Rect, icon: Painter, look: MarkLook) {
    val radius = box.width / 2
    val ring = box.width * CONTROL_RING_SHARE
    drawCircle(look.badgeFill, radius, box.center)
    clipPath(Path().apply { addOval(Rect(box.center, radius - ring)) }) {
        translate(box.left, box.top) { with(icon) { draw(Size(box.width, box.width)) } }
    }
    drawCircle(look.badgeRing, radius - ring / 2, box.center, style = Stroke(ring))
}
