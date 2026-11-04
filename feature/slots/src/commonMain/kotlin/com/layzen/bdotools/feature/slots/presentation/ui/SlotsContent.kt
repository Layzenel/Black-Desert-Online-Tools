package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.editor.EditorStatus
import com.layzen.bdotools.feature.slots.presentation.ui.editor.EditorToolbar
import com.layzen.bdotools.feature.slots.presentation.ui.editor.SlotsCanvas
import com.layzen.bdotools.feature.slots.presentation.ui.editor.rememberCanvasViewport
import com.layzen.bdotools.feature.slots.presentation.ui.sidebar.SlotsSidebar
import com.layzen.bdotools.uikit.theme.AppTheme

/** Окно после загрузки файла: настройки слева, холст редактора справа. */
@Composable
internal fun SlotsContent(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val viewport = rememberCanvasViewport()
    Row {
        SlotsSidebar(
            state,
            onEvent,
            Modifier
                .width(SlotsDimens.sidebarWidth)
                .heightIn(max = SlotsDimens.sidebarMaxHeight)
                .background(AppTheme.colors.backgroundSurface),
        )
        Column(
            Modifier.weight(1f).padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            EditorToolbar(state, onEvent, onFit = viewport::fit)
            SlotsCanvas(state, viewport, onEvent, Modifier.fillMaxWidth())
            EditorStatus(state)
        }
    }
}
