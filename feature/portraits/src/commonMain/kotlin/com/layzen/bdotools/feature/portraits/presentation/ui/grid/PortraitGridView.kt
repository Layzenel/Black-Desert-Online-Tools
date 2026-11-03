package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.uikit.components.HoverMenuBox

/**
 * Портреты на странице. Клик по портрету открывает его замену, при наведении появляется [PortraitMenu].
 */
@Composable
internal fun PortraitGridView(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    PortraitGridLayout(state.portraits) { portrait ->
        val id = portrait.characterId
        HoverMenuBox(
            onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.One(id))) },
            menu = { close -> PortraitMenu(id, hasBackups = state.hasBackups(id), close = close, onEvent = onEvent) },
        ) { menuOpen ->
            PortraitTile(portrait.image, contentDescription = id, selected = menuOpen)
        }
    }
}
