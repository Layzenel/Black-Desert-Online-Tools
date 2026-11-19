package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.uikit.icons.title
import org.jetbrains.compose.resources.StringResource
import java.awt.Dimension

/** Порядок щитов от правого края слота влево: самая сильная защита — с края. */
private val SHIELD_ORDER = listOf(SkillProtection.Invincible, SkillProtection.ForwardGuard, SkillProtection.SuperArmor)

/** Ширина щита — доля стороны слота: щиты растут вместе с «Размером интерфейса» игры. */
private const val SHIELD_SHARE = 0.3f

/** Высота щита к ширине (как у [drawShield]). */
private const val SHIELD_HEIGHT_RATIO = 15f / 13f

/** Отступ иконки от края слота (его рамка) — доля стороны слота: щиты стоят в углу самой иконки. */
private const val ICON_INSET_SHARE = 0.08f

/** Шаг щитов в ряду — доля ширины: соседние чуть заходят друг на друга. */
private const val STEP_SHARE = 0.85f

/** Диаметр круглого значка контроля — доля стороны слота. */
private const val CONTROL_SHARE = 0.38f

/** Насколько соседние значки заходят друг на друга — доля диаметра. */
private const val CONTROL_OVERLAP_SHARE = 0.08f

/**
 * Что рисуется на слоте [rect] и где: щиты [shields] в правом верхнем углу иконки, круглые значки контролей
 * [controls] по центру нижней кромки (наполовину ниже неё). Все координаты — экранные, в логических пикселях Java;
 * по ним и рисует окно щитов, и ищется значок под курсором для подсказки.
 */
internal data class SlotMarks(val rect: Rect, val shields: List<SkillProtection>, val controls: List<CrowdControl>) {

    /** Щиты по порядку от края слота: первый — самая сильная защита. */
    fun shieldRects(): List<Pair<SkillProtection, Rect>> {
        val width = rect.width * SHIELD_SHARE
        val inset = rect.width * ICON_INSET_SHARE
        val right = rect.right - inset
        return shields.mapIndexed { index, kind ->
            val left = right - width - index * width * STEP_SHARE
            kind to Rect(Offset(left, rect.top + inset), Size(width, width * SHIELD_HEIGHT_RATIO))
        }
    }

    /** Значки контролей слева направо, каждый — квадрат вокруг круга. */
    fun controlRects(): List<Pair<CrowdControl, Rect>> {
        val area = controlsArea() ?: return emptyList()
        val step = area.height * (1 - CONTROL_OVERLAP_SHARE)
        return controls.mapIndexed { index, control -> control to Rect(Offset(area.left + index * step, area.top), Size(area.height, area.height)) }
    }

    /** Ряд значков контролей: высота — диаметр значка; `null` — контролей нет. */
    fun controlsArea(): Rect? {
        if (controls.isEmpty()) return null
        val diameter = rect.width * CONTROL_SHARE
        val width = diameter + diameter * (1 - CONTROL_OVERLAP_SHARE) * (controls.size - 1)
        return Rect(Offset(rect.center.x - width / 2, rect.bottom - diameter / 2), Size(width, diameter))
    }

    /** Название защиты или контроля под точкой [point]; верхний из перекрытых значков — тот, что нарисован последним. */
    fun titleAt(point: Offset): StringResource? {
        controlRects().lastOrNull { (_, box) -> (box.center - point).getDistance() <= box.width / 2 }?.let { return it.first.title }
        return shieldRects().firstOrNull { (_, box) -> box.contains(point) }?.first?.title
    }

    companion object {
        /** Значки слота [slot] на экране [screen]; `null`, если ни защиты, ни контролей нет. */
        fun of(slot: ScreenSlot, protections: Set<SkillProtection>, controls: Set<CrowdControl>, screen: Dimension): SlotMarks? {
            val kinds = SHIELD_ORDER.filter { it in protections }
            val slotControls = CrowdControl.entries.filter { it in controls }
            if (kinds.isEmpty() && slotControls.isEmpty()) return null
            val side = (slot.size * screen.height).toFloat()
            val left = (slot.centerX * screen.width).toFloat() - side / 2
            val top = (slot.centerY * screen.height).toFloat() - side / 2
            return SlotMarks(Rect(Offset(left, top), Size(side, side)), kinds, slotControls)
        }
    }
}
