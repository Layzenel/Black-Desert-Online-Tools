package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Всплывающее окно списков вариантов: тёмная панель с рамкой и прямыми углами. Общее для [OptionsMenu] и [GroupedOptionsMenu].
 * Без [framed] окно прозрачное: панели внутри рисует содержимое ([menuPanel]), каждая своей высоты.
 */
@Composable
internal fun MenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    framed: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        // курсор, уехавший за край при перетаскивании полосы прокрутки, — не клик мимо списка
        onDismissRequest = { if (!ScrollbarDrag.blocksDismiss) onDismissRequest() },
        modifier = modifier,
        scrollState = scrollState,
        containerColor = if (framed) AppTheme.colors.backgroundRaised else Color.Transparent,
        shape = AppShapes.Sharp,
        tonalElevation = 0.dp,
        shadowElevation = if (framed) AppTheme.dimens.menuShadow else 0.dp,
        border = if (framed) BorderStroke(AppTheme.dimens.borderWidth, AppTheme.colors.border) else null,
        // фокусируемое окно в веб-сборке после закрытия съедает следующий клик по странице или модалке;
        // набор для поиска ловит [TypeToSearch] на странице
        properties = PopupProperties(focusable = false),
        content = content,
    )
}

/** Панель списка внутри прозрачного [MenuPopup]: та же подложка и рамка, что у обычного списка. */
@Composable
internal fun Modifier.menuPanel(): Modifier =
    background(AppTheme.colors.backgroundRaised).border(AppTheme.dimens.borderWidth, AppTheme.colors.border)

/**
 * Пункт выпадающего списка ([OptionsMenu], [GroupedOptionsMenu]): под курсором — подложка тёмного цвета акцента
 * темы окон (`titleBarDark`), а не серая подсветка Material3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MenuOption(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val hovered by interactionSource.collectIsHoveredAsState()
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        DropdownMenuItem(
            text = text,
            onClick = onClick,
            modifier = modifier.background(if (hovered) AppTheme.window.titleBarDark else Color.Transparent),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
        )
    }
}

/** Текст пункта списка: выбранный или раскрытый вариант подсвечен золотом. */
@Composable
internal fun MenuOptionText(text: String, highlighted: Boolean) {
    Text(
        text,
        style = AppTheme.typography.bodyBold,
        color = if (highlighted) AppTheme.colors.accentGold else AppTheme.colors.textPrimary,
    )
}
