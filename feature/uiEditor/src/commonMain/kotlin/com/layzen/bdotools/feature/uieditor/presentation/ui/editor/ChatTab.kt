package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.chatTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.chat_bring_in
import com.layzen.bdotools.feature.uieditor.resources.chat_take_out
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_bring_in
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_take_out
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Плашка чата [window] на рамке окна: название и кнопка, которая сразу переносит чат. У вкладки главного окна
 * ([combined]) — «Вынести» в отдельное окно, у отдельного окна — «Внести» во вкладку главного. Размер плашки
 * не зависит от увеличения холста, чтобы её было удобно нажимать.
 */
@Composable
internal fun ChatTab(window: Int, combined: Boolean, onMove: () -> Unit) {
    Row(
        Modifier
            .background(AppTheme.colors.backgroundRaised)
            .border(SlotsDimens.slotBorder, AppTheme.colors.textBeige)
            .padding(horizontal = AppTheme.dimens.spaceXs, vertical = SlotsDimens.chatTabPaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(chatTitle(window), style = AppTheme.typography.caption, color = AppTheme.colors.textBeige, maxLines = 1)
        FrameIconButton(
            painterResource(if (combined) Res.drawable.chat_take_out else Res.drawable.chat_bring_in),
            stringResource(if (combined) Res.string.slots_chat_take_out else Res.string.slots_chat_bring_in),
            SlotsDimens.chatTabIcon,
            onClick = onMove,
        )
    }
}
