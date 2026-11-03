package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.ui.grid.PortraitGridView
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_empty
import com.layzen.bdotools.feature.portraits.resources.portraits_grid_description
import com.layzen.bdotools.feature.portraits.resources.portraits_grid_title
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PortraitsContent(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    when {
        state.busy?.kind == BusyKind.Loading && state.portraits.isEmpty() -> Unit
        state.portraits.isEmpty() -> DescriptionText(stringResource(Res.string.portraits_empty))
        else -> {
            SectionHeader(
                stringResource(Res.string.portraits_grid_title, state.portraits.size),
                description = stringResource(Res.string.portraits_grid_description),
            )
            PortraitGridView(state, onEvent)
        }
    }
}
