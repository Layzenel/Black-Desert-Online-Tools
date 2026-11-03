package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Всплывающий список вариантов под элементом, к которому он привязан (ставится в тот же `Box`).
 * Выбранный вариант подсвечен золотом. [icon] — необязательная иконка слева от названия.
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
) {
    val colors = AppTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = colors.backgroundRaised,
        shape = AppShapes.Sharp,
        border = BorderStroke(AppTheme.dimens.borderWidth, colors.border),
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        label(option),
                        style = AppTheme.typography.bodyBold,
                        color = if (option == selected) colors.accentGold else colors.textPrimary,
                    )
                },
                leadingIcon = icon?.let { { it(option) } },
                onClick = {
                    onDismissRequest()
                    onSelect(option)
                },
            )
        }
    }
}
