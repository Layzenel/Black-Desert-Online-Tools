package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.layout.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.layout.FixedElement
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.presentation.ui.skill.SkillSlotTooltip
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import kotlin.math.roundToInt

/**
 * Редактор слотов на экране игры ([ScreenCanvas]): подложка, сетка от прицела, прицел и элементы интерфейса.
 * Клик по пустому месту и Esc снимают выбор, обводка выбирает элементы, стрелки их двигают, ПКМ открывает меню:
 * по невыбранному элементу — выбрав его, по выбранному — сделав его опорой выравнивания.
 */
@Composable
internal fun SlotsCanvas(state: SlotsState, viewport: CanvasViewport, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val screen = state.loaded?.game?.screen ?: return
    val focus = remember { FocusRequester() }
    val currentOnEvent by rememberUpdatedState(onEvent)
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    var menuAnchor by remember { mutableStateOf<SlotId?>(null) }
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
        onSecondaryClick = { at, point ->
            // Как в графических редакторах: ПКМ по невыбранному элементу выбирает его, по выбранному — делает опорой
            val hit = state.slots.lastOrNull { id -> state.layout[id]?.let { point.touches(id, it, screen) } == true }
            if (hit != null && hit !in state.selection) currentOnEvent(SlotsEvent.Select(hit, additive = false))
            menuAnchor = hit
            menuAt = at
        },
        overlay = {
            // Пустой Box в точке клика: меню раскрывается от его угла
            menuAt?.let { at ->
                // Новое меню в новой точке — заново, а закрытие старого не трогает уже открытое новое:
                // правый клик мимо меню и закрывает его, и открывает меню там, где кликнули
                key(at) {
                    Box(Modifier.offset { IntOffset(at.x.roundToInt(), at.y.roundToInt()) }) {
                        CanvasContextMenu(state, menuAnchor, onDismiss = { if (menuAt == at) menuAt = null }, onEvent = onEvent)
                    }
                }
            }
        },
    ) { scale ->
        CanvasContent(state, menuAnchor.takeIf { menuAt != null }, screen, scale, onEvent, onPress = { focus.requestFocus() })
    }
}

@Composable
private fun CanvasContent(
    state: SlotsState,
    anchor: SlotId?,
    screen: ScreenSpace,
    scale: Float,
    onEvent: (SlotsEvent) -> Unit,
    onPress: () -> Unit,
) {
    state.background?.takeIf { state.backgroundShown }?.let { BackgroundImage(it, state.backgroundDim) }
    GridLayer(state.grid, screen, scale, Modifier.fillMaxSize())
    CrosshairLayer(screen, scale, Modifier.fillMaxSize())
    FixedElement.entries.filter { it.isShownWith(state.layout.splitHp) }.forEach { FixedElementFrame(it, screen, scale) }
    // Чаты под слотами: слоты над ними остаются кликабельными. Вкладки главного окна рисует само главное окно
    val chats = state.layout.chats
    val tabs = chats.filterValues { it.combined }.keys.toList()
    chats.filterValues { !it.combined }.forEach { (window, chat) ->
        val windowTabs = if (window == ChatWindow.MAIN) tabs else emptyList()
        ChatFrame(window, chat, windowTabs, state.layout.isChatShown(window), window in state.selectedChats, screen, scale, onEvent, onPress)
    }
    state.slots.forEach { id ->
        val position = state.layout[id] ?: return@forEach
        // У слота с навыком — его надпись из игры; у пустого — заглушка надписи шириной в символах
        val skill = state.layout.skillAt(id)
        val memo = state.layout.memoAt(id)
        when {
            memo != null -> SlotLabelStub(id, position, screen, scale, memo.length.coerceIn(SlotLabel.CHARS_RANGE), state.layout.isVisible(id), memo)
            skill == null -> state.layout.labels[id]?.let { chars ->
                SlotLabelStub(id, position, screen, scale, chars, state.layout.isVisible(id))
            }
        }
        SlotFrame(
            id = id,
            position = position,
            screen = screen,
            scale = scale,
            selected = id in state.selection,
            anchor = id == anchor,
            visible = state.layout.isVisible(id),
            onEvent = onEvent,
            onPress = onPress,
            skillIcon = skill?.let(state.skills::iconOf),
            // Слоты, выбранные группой, тащатся вместе как слоты: навык по одному слоту — только без группы
            skillDraggable = skill != null && state.selection.size <= 1,
            pickSkillOnDoubleClick = id.group == SlotGroup.Cooldown && state.skills.catalog != null && state.skillsClass != null,
            skillTooltip = skill?.takeIf { state.skills.skill(it) != null && state.skillDrag == null }
                ?.let { skillNo -> { SkillSlotTooltip(skillNo, state.skills, onEvent) } },
            skillFrameAlpha = 1f - state.skillFrameTransparency / 100f,
        )
    }
    state.skillDrag?.let { drag ->
        state.layout.skillAt(drag.from)?.let(state.skills::iconOf)?.let { SkillDragGhost(drag, it, screen, scale) }
    }
    SnapGuidesLayer(state.snapGuides, screen, scale, Modifier.fillMaxSize())
}
