package com.layzen.bdotools.feature.portraits.presentation.ui.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.arrow_right
import com.layzen.bdotools.feature.portraits.resources.game_characters
import com.layzen.bdotools.feature.portraits.resources.portraits_notes_where_key
import com.layzen.bdotools.feature.portraits.resources.portraits_notes_where_map
import com.layzen.bdotools.feature.portraits.resources.portraits_notes_where_open
import com.layzen.bdotools.feature.portraits.resources.portraits_notes_where_window
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Где в игре увидеть заметки, одной строкой: «В игре: [M] карта → (кнопка персонажей) «Просмотр персонажей аккаунта»». */
@Composable
internal fun GameWindowHint() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundSurface)
            .border(dimens.borderWidth, colors.border)
            .padding(horizontal = dimens.spaceM, vertical = dimens.spaceS),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HintText(stringResource(Res.string.portraits_notes_where_open))
        Text(
            stringResource(Res.string.portraits_notes_where_key),
            style = AppTheme.typography.bodyBold,
            color = colors.textPrimary,
            modifier = Modifier
                .background(colors.backgroundItem, AppShapes.Small)
                .border(dimens.borderWidth, colors.buttonBorder, AppShapes.Small)
                .padding(horizontal = dimens.spaceS),
        )
        HintText(stringResource(Res.string.portraits_notes_where_map))
        Image(
            painterResource(Res.drawable.arrow_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.textMuted),
            modifier = Modifier.size(PortraitsDimens.gameButtonIconSize),
        )
        Box(
            Modifier
                .size(PortraitsDimens.gameButtonSize)
                .background(colors.titleBar, CircleShape)
                .border(dimens.borderWidth, colors.accentGoldPale, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(Res.drawable.game_characters),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.textPrimary),
                modifier = Modifier.size(PortraitsDimens.gameButtonIconSize),
            )
        }
        HintText(stringResource(Res.string.portraits_notes_where_window), Modifier.weight(1f, fill = false))
    }
}

@Composable
private fun HintText(text: String, modifier: Modifier = Modifier) = Text(
    text,
    style = AppTheme.typography.bodyBold,
    color = AppTheme.colors.textBeige,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier,
)
