package com.layzen.bdotools.uikit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

private const val SCRIM_ALPHA = 0.82f

/**
 * Элемент, поверх которого при наведении курсора появляются действия: элемент затемняется,
 * а по центру встаёт колонка пунктов ([ActionMenuItem], [ActionMenuDivider]).
 * Действия остаются внутри элемента и не перекрывают соседей, поэтому подходят для плиток в сетке.
 *
 * [content] получает `true`, пока курсор над элементом, чтобы его подсветить.
 */
@Composable
fun HoverActionsBox(
    actions: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(hovered: Boolean) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(modifier.hoverable(interaction)) {
        content(hovered)
        AnimatedVisibility(visible = hovered, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.matchParentSize()) {
            Column(
                Modifier
                    .background(AppTheme.colors.backgroundDeep.copy(alpha = SCRIM_ALPHA))
                    .padding(AppTheme.dimens.spaceXs),
                verticalArrangement = Arrangement.Center,
                content = actions,
            )
        }
    }
}
