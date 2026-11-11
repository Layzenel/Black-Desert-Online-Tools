package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.layout.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/** Окно чата в превью: рамка с названием [title]; неотмеченное приглушено, клик — [onToggle]. */
@Composable
internal fun PreviewChatFrame(chat: ChatWindow, title: String, screen: ScreenSpace, scale: Float, included: Boolean, onToggle: () -> Unit) {
    val density = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset((chat.left(screen) * scale).roundToInt(), (chat.top(screen) * scale).roundToInt()) }
            .size(with(density) { (chat.width * scale).toDp() }, with(density) { (chat.height * scale).toDp() })
            .alpha(if (included) 1f else SlotsDimens.HIDDEN_ALPHA)
            .background(AppTheme.colors.backgroundDeep.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA))
            .border(SlotsDimens.slotBorder, AppTheme.colors.textBeige)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, style = AppTheme.typography.caption, color = AppTheme.colors.textPrimary)
    }
}
