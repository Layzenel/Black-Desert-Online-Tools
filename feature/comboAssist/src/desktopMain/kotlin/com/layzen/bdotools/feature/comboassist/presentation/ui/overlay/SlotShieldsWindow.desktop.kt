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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.composable.overlay.rememberGameActive
import com.layzen.bdotools.composable.overlay.setClickThrough
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.uikit.icons.icon
import org.jetbrains.compose.resources.painterResource
import com.layzen.bdotools.uikit.theme.AppTheme
import java.awt.Toolkit

/** Порядок щитов от правого края слота влево: самая сильная защита — с края. */
private val SHIELD_ORDER = listOf(SkillProtection.Invincible, SkillProtection.ForwardGuard, SkillProtection.SuperArmor)

/** Ширина щита — доля стороны слота: щиты растут вместе с «Размером интерфейса» игры. */
private const val SHIELD_SHARE = 0.3f

/** Отступ иконки от края слота (его рамка) — доля стороны слота: щиты стоят в углу самой иконки. */
private const val ICON_INSET_SHARE = 0.08f

/** Диаметр круглого значка контроля — доля стороны слота. */
private const val CONTROL_SHARE = 0.38f

/** Насколько соседние значки заходят друг на друга — доля диаметра. */
private const val CONTROL_OVERLAP_SHARE = 0.08f

/** Толщина рамки значка — доля диаметра. */
private const val CONTROL_RING_SHARE = 0.07f

/** Шаг щитов в ряду — доля ширины: соседние чуть заходят друг на друга. */
private const val STEP_SHARE = 0.85f

/**
 * Окно щитов: прозрачное, без рамки и фокуса, поверх всех окон, клики сквозь него уходят в игру
 * ([setClickThrough]). Накрывает только слоты с защитой или контролями: щиты — в правом верхнем углу иконки, значки
 * контролей — круглые, в бежевой рамке, по центру нижней кромки слота (наполовину ниже неё); координаты слотов —
 * доли экрана, окно —
 * в логических пикселях Java, так масштаб Windows не мешает. Видно, только пока игра на переднем плане и слоты не
 * закрыты окном игры.
 */
@Composable
internal actual fun SlotShieldsWindow(
    slots: List<ScreenSlot>,
    protections: Map<Int, Set<SkillProtection>>,
    controls: Map<Int, Set<CrowdControl>>,
    shown: Boolean,
) {
    val screen = remember { Toolkit.getDefaultToolkit().screenSize }
    val marked = slots.mapNotNull { slot ->
        val kinds = SHIELD_ORDER.filter { it in protections[slot.skillNo].orEmpty() }
        val slotControls = CrowdControl.entries.filter { it in controls[slot.skillNo].orEmpty() }
        if (kinds.isEmpty() && slotControls.isEmpty()) return@mapNotNull null
        val side = (slot.size * screen.height).toFloat()
        val left = (slot.centerX * screen.width).toFloat() - side / 2
        val top = (slot.centerY * screen.height).toFloat() - side / 2
        SlotMarks(Rect(Offset(left, top), Size(side, side)), kinds, slotControls)
    }
    if (marked.isEmpty()) return
    val bounds = marked.flatMap { listOfNotNull(it.rect, it.controlsArea()) }.reduce { a, b -> Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom)) }
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
    val badgeFill = AppTheme.colors.backgroundDeep
    val badgeRing = AppTheme.colors.accentGoldPale
    val controlIcons = CrowdControl.entries.associateWith { painterResource(it.icon) }
    Window(
        onCloseRequest = {},
        state = state,
        visible = gameActive && shown,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(window) { setClickThrough(window, enabled = true) }
        Canvas(Modifier.fillMaxSize()) {
            marked.forEach { (rect, kinds, slotControls) ->
                val width = (rect.width * SHIELD_SHARE).dp.toPx()
                val inset = (rect.width * ICON_INSET_SHARE).dp.toPx()
                val right = (rect.right - bounds.left).dp.toPx() - inset
                val top = (rect.top - bounds.top).dp.toPx() + inset
                // самый сильный щит у края рисуется последним, поверх соседей
                kinds.indices.reversed().forEach { index ->
                    val left = right - width - index * width * STEP_SHARE
                    drawShield(Offset(left, top), width, colors.getValue(kinds[index]), outline)
                }
                val area = SlotMarks(rect, kinds, slotControls).controlsArea() ?: return@forEach
                val diameter = area.height.dp.toPx()
                val step = diameter * (1 - CONTROL_OVERLAP_SHARE)
                val ring = diameter * CONTROL_RING_SHARE
                val rowLeft = (area.left - bounds.left).dp.toPx()
                val rowTop = (area.top - bounds.top).dp.toPx()
                slotControls.forEachIndexed { index, control ->
                    val center = Offset(rowLeft + index * step + diameter / 2, rowTop + diameter / 2)
                    drawCircle(badgeFill, diameter / 2, center)
                    val circle = Path().apply { addOval(Rect(center, diameter / 2 - ring)) }
                    clipPath(circle) {
                        translate(center.x - diameter / 2, center.y - diameter / 2) { with(controlIcons.getValue(control)) { draw(Size(diameter, diameter)) } }
                    }
                    drawCircle(badgeRing, diameter / 2 - ring / 2, center, style = Stroke(ring))
                }
            }
        }
    }
}

/** Что рисуется на слоте [rect]: щиты [shields] и значки контролей [controls]. */
private data class SlotMarks(val rect: Rect, val shields: List<SkillProtection>, val controls: List<CrowdControl>) {
    /**
     * Ряд круглых значков контролей в координатах экрана: по центру слота, центры значков — на его нижней кромке;
     * высота ряда — диаметр значка. `null` — контролей нет.
     */
    fun controlsArea(): Rect? {
        if (controls.isEmpty()) return null
        val diameter = rect.width * CONTROL_SHARE
        val width = diameter + diameter * (1 - CONTROL_OVERLAP_SHARE) * (controls.size - 1)
        return Rect(Offset(rect.center.x - width / 2, rect.bottom - diameter / 2), Size(width, diameter))
    }
}
