package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.layzen.bdotools.feature.uieditor.domain.layout.CHAT_MIN_HEIGHT
import com.layzen.bdotools.feature.uieditor.domain.layout.CHAT_MIN_WIDTH
import com.layzen.bdotools.feature.uieditor.domain.layout.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.chatTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_height
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_size_description
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_width
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_x
import com.layzen.bdotools.feature.uieditor.resources.slots_slot_y
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueText
import org.jetbrains.compose.resources.stringResource

/** Самое большое число, которое можно набрать в поле размера: пределы окна проверяются, а не прижимают набор. */
private const val MAX_TYPED = 9999

/**
 * Выбранное окно чата [window]: центр от прицела, как у слотов, и размер в UI-px. Размер применяется, как только
 * набранное число попадает в пределы окна (их показывает пояснение): «2» на пути к «250» окно не сжимает.
 */
@Composable
internal fun ChatInspector(window: Int, chat: ChatWindow, screen: ScreenSpace, onEvent: (SlotsEvent) -> Unit) {
    val crosshair = screen.crosshair
    val maxWidth = ChatWindow.maxWidth(screen)
    val maxHeight = screen.height.toInt()
    ValueText(chatTitle(window))
    CoordinateRow(stringResource(Res.string.slots_slot_x), chat.center.x - crosshair.x) {
        onEvent(SlotsEvent.SetChatPosition(window, chat.center.copy(x = it + crosshair.x)))
    }
    CoordinateRow(stringResource(Res.string.slots_slot_y), chat.center.y - crosshair.y) {
        onEvent(SlotsEvent.SetChatPosition(window, chat.center.copy(y = it + crosshair.y)))
    }
    SizeRow(stringResource(Res.string.slots_chat_width), chat.width, CHAT_MIN_WIDTH..maxWidth) {
        onEvent(SlotsEvent.SetChatSize(window, it, chat.height))
    }
    SizeRow(stringResource(Res.string.slots_chat_height), chat.height, CHAT_MIN_HEIGHT..maxHeight) {
        onEvent(SlotsEvent.SetChatSize(window, chat.width, it))
    }
    DescriptionText(stringResource(Res.string.slots_chat_size_description, CHAT_MIN_WIDTH, maxWidth, CHAT_MIN_HEIGHT, maxHeight))
}

/** Поле размера: набор свободный, в окно уходит только число в пределах [limits]. */
@Composable
private fun SizeRow(label: String, value: Int, limits: IntRange, onValueChange: (Int) -> Unit) {
    var typed by remember(value) { mutableIntStateOf(value) }
    CoordinateRow(label, typed, range = 0..MAX_TYPED) {
        typed = it
        if (it in limits) onValueChange(it)
    }
}
