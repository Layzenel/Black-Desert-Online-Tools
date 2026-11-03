package com.layzen.bdotools.uikit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import com.layzen.bdotools.uikit.theme.AppTheme

private const val SCRIM_ALPHA = 0.7f

/**
 * Элемент, поверх которого при наведении курсора появляются действия: элемент размывается и затемняется,
 * обводится золотой рамкой, а по центру встаёт колонка пунктов ([ActionMenuItem], [ActionMenuDivider]).
 * Размытие не даёт надписям и значкам элемента мешать читать пункты; рамка рисуется поверх и остаётся чёткой.
 * Действия остаются внутри элемента и не перекрывают соседей, поэтому подходят для плиток в сетке.
 */
@Composable
fun HoverActionsBox(
    actions: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val blur by animateDpAsState(if (hovered) AppTheme.dimens.hoverBlurRadius else 0.dp)
    Box(modifier.hoverable(interaction), propagateMinConstraints = true) {
        Box(Modifier.blur(blur), propagateMinConstraints = true, content = content)
        AnimatedVisibility(visible = hovered, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.matchParentSize()) {
            Column(
                Modifier
                    .background(AppTheme.colors.backgroundDeep.copy(alpha = SCRIM_ALPHA))
                    .border(AppTheme.dimens.selectedBorderWidth, AppTheme.colors.accentGoldPale)
                    .padding(AppTheme.dimens.spaceXs),
                verticalArrangement = Arrangement.Center,
                content = actions,
            )
        }
    }
}
