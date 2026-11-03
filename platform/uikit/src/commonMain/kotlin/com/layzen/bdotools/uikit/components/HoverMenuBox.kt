package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/** Сколько ждать перед закрытием меню, чтобы курсор успел перейти с элемента на меню. */
private const val HOVER_CLOSE_DELAY_MS = 200L

/**
 * Элемент с выпадающим меню по наведению. Меню появляется справа от элемента и немного заходит на него,
 * поэтому курсор переходит на меню без разрыва. Если справа нет места, меню открывается слева.
 *
 * Клик по элементу вызывает [onClick] — основное действие. Если его нет, клик открывает и закрывает меню,
 * чтобы им можно было пользоваться и без мыши.
 * [content] получает `true`, пока меню открыто, чтобы подсветить элемент.
 */
@Composable
fun HoverMenuBox(
    menu: @Composable ColumnScope.(close: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.(menuOpen: Boolean) -> Unit,
) {
    val coordinator = LocalHoverMenuCoordinator.current
    val token = remember { Any() }
    val anchorInteraction = remember { MutableInteractionSource() }
    val menuInteraction = remember { MutableInteractionSource() }
    val anchorHovered by anchorInteraction.collectIsHoveredAsState()
    val menuHovered by menuInteraction.collectIsHoveredAsState()
    val hovered = enabled && (anchorHovered || menuHovered)

    LaunchedEffect(hovered) {
        if (hovered) {
            coordinator.owner = token
        } else if (coordinator.owner === token) {
            delay(HOVER_CLOSE_DELAY_MS)
            if (coordinator.owner === token) coordinator.owner = null
        }
    }
    val visible = enabled && coordinator.owner === token
    val close = { if (coordinator.owner === token) coordinator.owner = null }

    Box(
        modifier
            .hoverable(anchorInteraction, enabled)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(remember { MutableInteractionSource() }, indication = null, enabled = enabled) {
                if (onClick != null) {
                    close()
                    onClick()
                } else {
                    coordinator.owner = if (visible) null else token
                }
            },
    ) {
        content(visible)
        if (visible) {
            val overlap = with(LocalDensity.current) { AppTheme.dimens.spaceS.roundToPx() }
            Popup(
                popupPositionProvider = remember(overlap) { SideMenuPositionProvider(overlap) },
                onDismissRequest = close,
                properties = PopupProperties(focusable = false),
            ) {
                Column(
                    Modifier
                        .hoverable(menuInteraction)
                        .width(AppTheme.dimens.popupMenuWidth)
                        .clip(AppShapes.Menu)
                        .background(AppTheme.colors.backgroundRaised)
                        .border(AppTheme.dimens.borderWidth, AppTheme.colors.borderSelected, AppShapes.Menu),
                ) {
                    menu(close)
                }
            }
        }
    }
}
