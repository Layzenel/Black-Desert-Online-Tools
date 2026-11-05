package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_background
import com.layzen.bdotools.feature.slots.resources.slots_background_description
import com.layzen.bdotools.feature.slots.resources.slots_background_hide
import com.layzen.bdotools.feature.slots.resources.slots_background_pick
import com.layzen.bdotools.feature.slots.resources.slots_background_show
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Своя картинка под редактором — например, скриншот из игры с персонажем. */
@Composable
internal fun BackgroundSettings(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(
            stringResource(Res.string.slots_background),
            description = stringResource(Res.string.slots_background_description),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            ActionButton(stringResource(Res.string.slots_background_pick), onClick = { onEvent(SlotsEvent.PickBackground) })
            if (state.background != null) {
                ActionButton(
                    stringResource(if (state.backgroundShown) Res.string.slots_background_hide else Res.string.slots_background_show),
                    onClick = { onEvent(SlotsEvent.ToggleBackground) },
                )
            }
        }
    }
}
