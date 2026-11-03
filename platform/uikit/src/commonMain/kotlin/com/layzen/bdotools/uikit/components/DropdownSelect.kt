package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Выпадающий список: тёмное поле со значением по центру и треугольником справа. */
@Composable
fun <T> DropdownSelect(
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(Dp.Unspecified) }
    val density = LocalDensity.current
    Box(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidth = with(density) { it.width.toDp() } }
                .heightIn(min = dimens.inputHeight)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(remember { MutableInteractionSource() }, null, role = Role.DropdownList) { expanded = true }
                .background(colors.backgroundItem)
                .border(dimens.borderWidth, colors.border)
                .padding(horizontal = dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label(selected),
                style = AppTheme.typography.bodyBold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            // Стандартный шрифт web-сборки не содержит символов вроде ▼, поэтому стрелка рисуется
            Canvas(Modifier.size(width = dimens.dropdownArrowWidth, height = dimens.dropdownArrowHeight)) {
                drawPath(
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2, size.height)
                        close()
                    },
                    colors.textBeige,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = if (anchorWidth != Dp.Unspecified) Modifier.width(anchorWidth) else Modifier,
            containerColor = colors.backgroundRaised,
            shape = AppShapes.Sharp,
            border = BorderStroke(dimens.borderWidth, colors.border),
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
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
