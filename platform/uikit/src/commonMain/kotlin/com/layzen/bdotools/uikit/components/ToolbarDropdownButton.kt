package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * [ToolbarButton] со списком вариантов под ним: пока курсор над кнопкой или над списком, под кнопкой открыта панель
 * с названием [description] и рядом кнопок [variants] (обычно [ToolbarButton] с картинкой варианта). Клик по самой
 * кнопке — [onClick]. Подсказки у кнопки нет: название показывает панель. Список не перехватывает фокус.
 */
@Composable
fun ToolbarDropdownButton(
    icon: Painter,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    variants: @Composable RowScope.() -> Unit,
) {
    val buttonHover = remember { MutableInteractionSource() }
    val panelHover = remember { MutableInteractionSource() }
    val buttonHovered by buttonHover.collectIsHoveredAsState()
    val panelHovered by panelHover.collectIsHoveredAsState()
    var open by remember { mutableStateOf(false) }
    // пауза перед закрытием — курсор успевает перейти с кнопки на панель
    LaunchedEffect(buttonHovered, panelHovered) {
        if (buttonHovered || panelHovered) {
            open = true
        } else {
            delay(SUBMENU_CLOSE_DELAY_MS)
            open = false
        }
    }
    val dimens = AppTheme.dimens
    Box(modifier.hoverable(buttonHover)) {
        ToolbarButton(icon, description, onClick, selected = selected, hint = false)
        if (open) {
            Popup(popupPositionProvider = BelowAnchor, properties = PopupProperties(focusable = false)) {
                Column(
                    Modifier.hoverable(panelHover).shadow(dimens.menuShadow).menuPanel().padding(dimens.spaceXs),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
                ) {
                    Text(description, style = AppTheme.typography.caption, color = AppTheme.colors.textBeige)
                    Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs), content = variants)
                }
            }
        }
    }
}

/** Панель встаёт под кнопкой от её левого края; у правого края окна сдвигается влево. */
private object BelowAnchor : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize) =
        IntOffset(
            anchorBounds.left.coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0),
            anchorBounds.bottom,
        )
}
