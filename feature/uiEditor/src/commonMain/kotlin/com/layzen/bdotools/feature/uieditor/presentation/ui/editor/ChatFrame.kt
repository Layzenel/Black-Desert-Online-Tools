package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.SlotGroup
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.chatTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_size
import com.layzen.bdotools.uikit.components.resizeCursor
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Окно чата [window] на холсте в настоящем размере, как в режиме правки интерфейса игры: глазик сверху по центру,
 * в центре название и размер в UI-px, справа сверху ручка, за которую окно растягивают (левый нижний угол стоит
 * на месте). Окно перетаскивается целиком. [shown] — видно ли окно в игре с учётом главного чата: невидимое
 * приглушено. Слева сверху плашки [ChatTab]: у главного окна — его вкладки [tabs] (`WindowIndex`) с «Вынести»,
 * у отдельного — оно само с «Внести».
 */
@Composable
internal fun ChatFrame(
    window: Int,
    chat: ChatWindow,
    tabs: List<Int>,
    shown: Boolean,
    screen: ScreenSpace,
    scale: Float,
    onEvent: (SlotsEvent) -> Unit,
) {
    val density = LocalDensity.current
    val color = AppTheme.colors.textBeige
    // Значки такого же размера, как глазик у слота: растут вместе с увеличением холста
    val iconSize = with(density) { (SlotGroup.Cooldown.width * scale).toDp() } * SlotsDimens.EYE_SHARE
    Box(
        Modifier
            .offset { IntOffset((chat.left(screen) * scale).roundToInt(), (chat.top(screen) * scale).roundToInt()) }
            .size(with(density) { (chat.width * scale).toDp() }, with(density) { (chat.height * scale).toDp() })
            .alpha(if (shown) 1f else SlotsDimens.HIDDEN_ALPHA)
            .background(AppTheme.colors.backgroundDeep.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA))
            .border(SlotsDimens.slotBorder, color)
            .pointerHoverIcon(PointerIcon.Hand)
            .chatDrag(scale, onEvent) { dx, dy -> SlotsEvent.MoveChat(window, dx, dy) },
    ) {
        val inset = iconSize * SlotsDimens.EYE_TOP_SHARE
        // Глаз сверху по центру, как у слотов и в режиме правки интерфейса игры
        EyeToggle(
            chat.visible,
            iconSize,
            onToggle = { onEvent(SlotsEvent.ToggleChatVisibility(window)) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = inset),
        )
        // Плашки слева сверху: у главного окна — его вкладки с «Вынести», у отдельного — оно само с «Внести»
        Row(
            Modifier.align(Alignment.TopStart).padding(AppTheme.dimens.spaceXs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        ) {
            if (window == ChatWindow.MAIN) {
                tabs.forEach { tab -> ChatTab(tab, combined = true, onMove = { onEvent(SlotsEvent.DetachChat(tab)) }) }
            } else {
                ChatTab(window, combined = false, onMove = { onEvent(SlotsEvent.CombineChat(window)) })
            }
        }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(chatTitle(window), style = AppTheme.typography.caption, color = AppTheme.colors.textPrimary)
            // Клик по размеру — ввод размера руками
            Text(
                stringResource(Res.string.slots_chat_size, chat.width, chat.height),
                modifier = Modifier.consumingTap { onEvent(SlotsEvent.OpenChatSize(window)) },
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textBeige,
                textDecoration = TextDecoration.Underline,
            )
        }
        ResizeHandle(window, scale, onEvent, Modifier.align(Alignment.TopEnd))
    }
}

/** Ручка в правом верхнем углу: только за неё окно чата растягивается, как в игре. */
@Composable
private fun ResizeHandle(window: Int, scale: Float, onEvent: (SlotsEvent) -> Unit, modifier: Modifier) {
    var dragging by remember { mutableStateOf(false) }
    Box(
        modifier
            .size(SlotsDimens.chatHandle)
            .background(AppTheme.colors.textBeige)
            .resizeCursor(dragging)
            .chatDrag(scale, onEvent, onDragging = { dragging = it }) { dx, dy -> SlotsEvent.ResizeChat(window, dx, dy) },
    )
}
