package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Всплывающий список вариантов под элементом, к которому он привязан (ставится в тот же `Box`).
 * Выбранный вариант подсвечен золотом. [icon] — необязательная иконка слева от названия, [trailing] — справа
 * (например, кнопка удаления варианта: её нажатие не выбирает вариант).
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
                trailingIcon = trailing?.let { { it(option) } },
                onClick = {
                    onDismissRequest()
                    onSelect(option)
                },
            )
        }
    }
}
