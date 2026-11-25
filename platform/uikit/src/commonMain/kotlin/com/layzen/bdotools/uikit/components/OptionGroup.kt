package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.layzen.bdotools.uikit.theme.AppTheme

/** Ряд взаимоисключающих вариантов одинаковой ширины; [icon] — картинка варианта вместо подписи ([OptionButton]). */
@Composable
fun <T> OptionGroup(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    icon: @Composable (T) -> Painter? = { null },
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        options.forEach { option ->
            OptionButton(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
                icon = icon(option),
            )
        }
    }
}
