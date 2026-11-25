package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import com.layzen.bdotools.uikit.theme.AppTheme
import com.layzen.bdotools.uikit.theme.CloseIcon

/** Насколько приглушён рисунок темы, пока курсор не над кнопкой. */
private const val IDLE_ALPHA = 0.85f

/**
 * Кнопка закрытия в шапке окна: рисунок зависит от темы окон ([AppTheme.window]) — крестик, морская звезда, снежинка,
 * клевер или цветок, как в игре. В классической теме — обычный [CloseButton].
 */
@Composable
fun WindowCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val style = AppTheme.window
    if (style.closeIcon == CloseIcon.Cross) {
        CloseButton(onClick, modifier)
        return
    }
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        modifier
            .size(dimens.iconSize)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(dimens.iconSize).alpha(if (hovered) 1f else IDLE_ALPHA)) {
            drawCloseIcon(style.closeIcon, style.closeColor, style.closeAccent)
        }
    }
}
