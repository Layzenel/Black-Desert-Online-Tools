package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.uieditor.presentation.ui.skill.SkillIcon
import com.layzen.bdotools.uikit.components.MenuItem

/**
 * Навык в списке выбора: иконка и название, клик — [onPick]. [onTooltip] говорит, показывать ли его тултип:
 * курсор над иконкой или над строкой с зажатым Alt ([altPressed]).
 */
@Composable
internal fun SkillChoice(
    name: String,
    icon: ImageBitmap?,
    altPressed: Boolean,
    onPick: () -> Unit,
    onTooltip: (Boolean) -> Unit,
) {
    val row = remember { MutableInteractionSource() }
    val iconArea = remember { MutableInteractionSource() }
    val rowHovered by row.collectIsHoveredAsState()
    val iconHovered by iconArea.collectIsHoveredAsState()
    val tooltip = iconHovered || (rowHovered && altPressed)
    DisposableEffect(tooltip) {
        onTooltip(tooltip)
        // Строка пропала из списка (поиск) — её тултип тоже
        onDispose { if (tooltip) onTooltip(false) }
    }
    Box(Modifier.hoverable(row)) {
        MenuItem(name, onClick = onPick, icon = { Box(Modifier.hoverable(iconArea)) { SkillIcon(icon) } })
    }
}
