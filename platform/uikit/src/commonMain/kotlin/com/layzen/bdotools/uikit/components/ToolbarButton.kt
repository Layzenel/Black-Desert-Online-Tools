package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Квадратная кнопка-иконка панели инструментов — [ActionButton] без подписи, с подсказкой [description] при наведении.
 * [selected] — включённый режим ([ActionButtonState.Toggle]: коричневая, золотая иконка), [enabled] = `false` —
 * серая иконка, клик не срабатывает. [icon] — белый силуэт, цвет ему задаёт кнопка.
 */
@Composable
fun ToolbarButton(
    icon: Painter,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
) {
    val state = if (selected) {
        ActionButtonState.Toggle(text = "", checked = true, icon = icon, enabled = enabled, onClick = onClick)
    } else {
        ActionButtonState.Neutral(text = "", icon = icon, enabled = enabled, onClick = onClick)
    }
    HoverTooltip(description, modifier) {
        ActionButton(state, Modifier.semantics { contentDescription = description })
    }
}
