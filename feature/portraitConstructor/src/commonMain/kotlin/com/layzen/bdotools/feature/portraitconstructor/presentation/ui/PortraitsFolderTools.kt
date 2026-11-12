package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_color
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_folder
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_reload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_colors_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_folder_change_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reload_hint
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Значки справа от заголовка страницы: «Цвет заметок и значков всех портретов», «Сменить папку» и «Обновить» — нужны
 * реже действий внизу и места в их строке не занимают.
 */
@Composable
internal fun PortraitsFolderTools(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val idle = state.loading == null
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        ToolbarButton(
            painterResource(Res.drawable.menu_color),
            stringResource(Res.string.portraits_colors_hint),
            onClick = { onEvent(PortraitsEvent.OpenDecorationColors) },
            enabled = idle && state.portraits.isNotEmpty(),
        )
        ToolbarButton(
            painterResource(Res.drawable.menu_folder),
            stringResource(Res.string.portraits_folder_change_hint),
            onClick = { onEvent(PortraitsEvent.SelectFolder) },
            enabled = idle,
        )
        ToolbarButton(
            painterResource(Res.drawable.menu_reload),
            stringResource(Res.string.portraits_reload_hint),
            onClick = { onEvent(PortraitsEvent.Reload) },
            enabled = idle,
        )
    }
}
