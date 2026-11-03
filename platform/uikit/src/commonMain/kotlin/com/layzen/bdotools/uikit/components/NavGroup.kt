package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Вертикальная навигация как в окне настроек: группы с иконкой и вложенные пункты,
 * соединённые тонкой линией слева. Активный вложенный пункт золотой, остальные серые.
 */
@Composable
fun NavGroup(
    title: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    children: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.navItemHeight)
                .then(
                    if (onClick != null) {
                        Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(remember { MutableInteractionSource() }, null, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
        ) {
            Box(Modifier.size(dimens.iconSize), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalContentColor provides colors.textBeige) { icon?.invoke() }
            }
            Text(
                title,
                style = AppTheme.typography.sectionTitle,
                color = if (selected) colors.accentGold else colors.textBeige,
            )
        }
        if (children != null) {
            Column(
                Modifier
                    .padding(start = dimens.spaceM + dimens.iconSize / 2)
                    .drawBehind {
                        drawLine(colors.borderSelected, Offset.Zero, Offset(0f, size.height), dimens.accentLineWidth.toPx())
                    },
            ) {
                children()
            }
        }
    }
}
