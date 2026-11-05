package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.menu_hide
import com.layzen.bdotools.feature.slots.resources.menu_image
import com.layzen.bdotools.feature.slots.resources.menu_show
import com.layzen.bdotools.feature.slots.resources.slots_background
import com.layzen.bdotools.feature.slots.resources.slots_background_description
import com.layzen.bdotools.feature.slots.resources.slots_background_hide
import com.layzen.bdotools.feature.slots.resources.slots_background_pick
import com.layzen.bdotools.feature.slots.resources.slots_background_show
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Своя картинка под редактором — например, скриншот из игры с персонажем — и её затенение. */
@Composable
internal fun BackgroundSettings(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(
            stringResource(Res.string.slots_background),
            description = stringResource(Res.string.slots_background_description),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            MenuAction(
                stringResource(Res.string.slots_background_pick),
                onClick = { onEvent(SlotsEvent.PickBackground) },
                modifier = Modifier.weight(1f),
                icon = painterResource(Res.drawable.menu_image),
            )
            if (state.background != null) {
                MenuAction(
                    stringResource(if (state.backgroundShown) Res.string.slots_background_hide else Res.string.slots_background_show),
                    onClick = { onEvent(SlotsEvent.ToggleBackground) },
                    modifier = Modifier.weight(1f),
                    icon = painterResource(if (state.backgroundShown) Res.drawable.menu_hide else Res.drawable.menu_show),
                )
            }
        }
        if (state.background != null && state.backgroundShown) BackgroundDimSlider(state.backgroundDim, onEvent)
    }
}
