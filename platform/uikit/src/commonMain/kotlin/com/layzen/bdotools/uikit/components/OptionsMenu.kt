package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MenuDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Всплывающий список вариантов под элементом, к которому он привязан (ставится в тот же `Box`).
 * Выбранный вариант подсвечен золотом. [icon] — необязательная иконка слева от названия, [trailing] — справа
 * (например, кнопка удаления варианта: её нажатие не выбирает вариант). Его центр стоит на оси стрелки [DropdownSelect],
 * если список шириной с поле.
 * Список не выше `optionsMenuMaxHeight`; длинный прокручивается, справа видна полоса прокрутки.
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
) {
    val scroll = rememberScrollState()
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
        options.forEach { option ->
            DropdownMenuItem(
                text = { MenuOptionText(label(option), highlighted = option == selected) },
                leadingIcon = icon?.let { { it(option) } },
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
