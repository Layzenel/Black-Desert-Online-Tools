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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/** Сколько ждать перед закрытием меню, чтобы курсор успел перейти с элемента на меню. */
private const val HOVER_CLOSE_DELAY_MS = 200L

/**
 * Какое меню сейчас открыто. На странице открыто не больше одного меню: наведение на другой элемент
 * сразу закрывает предыдущее, поэтому при быстром движении курсора меню не копятся.
 */
@Stable
class HoverMenuCoordinator {
    internal var owner: Any? by mutableStateOf(null)
}

val LocalHoverMenuCoordinator = staticCompositionLocalOf { HoverMenuCoordinator() }

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

/** Пункт меню [HoverMenuBox]. */
@Composable
fun PopupMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val dimens = AppTheme.dimens
    // Кликается вся строка, а подсветка — скруглённая плашка с отступом, чтобы не прилипать к разделителям
    Box(
        modifier
            .fillMaxWidth()
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = dimens.spaceXs, vertical = dimens.popupMenuItemInset),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.popupMenuItemHeight - dimens.popupMenuItemInset * 2)
                .background(if (hovered && enabled) colors.backgroundSelected else Color.Transparent, AppShapes.MenuItem)
                .padding(horizontal = dimens.spaceS),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text,
                style = AppTheme.typography.bodyBold,
                color = when {
                    !enabled -> colors.textDisabled
                    hovered -> colors.accentGold
                    else -> colors.textPrimary
                },
                maxLines = 1,
            )
        }
    }
}

/** Тонкая линия между пунктами меню. */
@Composable
fun PopupMenuDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimens.spaceS)
            .height(AppTheme.dimens.borderWidth)
            .background(AppTheme.colors.buttonBorder),
    )
}

/** Справа от элемента с заходом [overlap]; если не помещается — слева. По вертикали — от верха элемента. */
private class SideMenuPositionProvider(private val overlap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val right = anchorBounds.right - overlap
        val x = if (right + popupContentSize.width <= windowSize.width) {
            right
        } else {
            (anchorBounds.left + overlap - popupContentSize.width).coerceAtLeast(0)
        }
        val y = anchorBounds.top.coerceAtMost((windowSize.height - popupContentSize.height).coerceAtLeast(0))
        return IntOffset(x, y)
    }
}
