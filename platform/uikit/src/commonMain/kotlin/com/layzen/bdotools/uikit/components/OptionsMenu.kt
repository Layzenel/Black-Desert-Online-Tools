package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MenuDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Всплывающий список вариантов под элементом, к которому он привязан (ставится в тот же `Box`).
 * Выбранный вариант подсвечен золотом. [icon] — необязательная иконка слева от названия, [trailing] — справа
 * (например, кнопка удаления варианта: её нажатие не выбирает вариант). Его центр стоит на оси стрелки [DropdownSelect],
 * если список шириной с поле.
 * Список не выше `optionsMenuMaxHeight`; длинный прокручивается, справа видна полоса прокрутки.
 * С [searchPlaceholder] сверху строка поиска: печатать можно сразу ([TypeToSearch]), видны только варианты,
 * в названии которых есть набранное (без учёта регистра).
 * С [query] набор тоже ловится сразу, но строку поиска показывает и варианты фильтрует сам вызывающий (поле поиска над
 * списком): набранное уходит в [onQueryChange].
 * С [onMove] пункты можно переставлять мышью за значок [DragHandle] слева (пока строка поиска пуста).
 */
@Composable
fun <T> OptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    icon: (@Composable (T) -> Unit)? = null,
    trailing: (@Composable (T) -> Unit)? = null,
    searchPlaceholder: String? = null,
    query: String? = null,
    onQueryChange: (String) -> Unit = {},
    onMove: ((from: Int, to: Int) -> Unit)? = null,
) {
    val scroll = rememberScrollState()
    var ownQuery by remember(expanded) { mutableStateOf("") }
    val text = ownQuery.trim()
    val shown = if (text.isEmpty()) options else options.filter { label(it).contains(text, ignoreCase = true) }
    when {
        query != null -> TypeToSearch(expanded, query, onQueryChange, onEscape = onDismissRequest)
        searchPlaceholder != null -> TypeToSearch(expanded, ownQuery, { ownQuery = it }, onEscape = onDismissRequest)
    }
    val reorder = rememberReorderState { from, to -> onMove?.invoke(from, to) }
    val movable = onMove != null && text.isEmpty()
    val dimens = AppTheme.dimens
    // Конец пункта — так, чтобы середина элемента справа совпала с серединой стрелки поля
    val trailingPadding = PaddingValues(
        start = MenuDefaults.DropdownMenuItemContentPadding.calculateStartPadding(LayoutDirection.Ltr),
        end = dimens.spaceM + dimens.dropdownArrowWidth / 2 - dimens.menuTrailingWidth / 2,
    )
    MenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.heightIn(max = AppTheme.dimens.optionsMenuMaxHeight).verticalScrollbar(scroll),
        scrollState = scroll,
    ) {
        if (query == null && searchPlaceholder != null) {
            MenuSearchLine(ownQuery, searchPlaceholder, Modifier.fillMaxWidth(), onClear = { ownQuery = "" })
        }
        shown.forEachIndexed { index, option ->
            MenuOption(
                text = { MenuOptionText(label(option), highlighted = option == selected) },
                modifier = if (movable) Modifier.reorderItem(reorder, index) else Modifier,
                leadingIcon = when {
                    movable -> { { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.spaceS)) { DragHandle(reorder, index); icon?.invoke(option) } } }
                    else -> icon?.let { { it(option) } }
                },
                trailingIcon = trailing?.let {
                    { Box(Modifier.width(dimens.menuTrailingWidth), contentAlignment = Alignment.Center) { it(option) } }
                },
                contentPadding = if (trailing == null) MenuDefaults.DropdownMenuItemContentPadding else trailingPadding,
                onClick = {
                    onDismissRequest()
                    onSelect(option)
                },
            )
        }
    }
}
