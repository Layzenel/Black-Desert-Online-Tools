package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.uikit.components.HoverActionsBox

/**
 * Портреты на странице. При наведении на портрет поверх него появляются действия [PortraitMenu];
 * сам портрет на клик не реагирует.
 */
@Composable
internal fun PortraitGridView(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    PortraitGridLayout(state.portraits) { portrait ->
        val id = portrait.characterId
        HoverActionsBox(
            actions = { PortraitMenu(id, hasBackups = state.hasBackups(id), onEvent = onEvent) },
        ) {
            PortraitTile(portrait.image, contentDescription = id)
        }
    }
}
