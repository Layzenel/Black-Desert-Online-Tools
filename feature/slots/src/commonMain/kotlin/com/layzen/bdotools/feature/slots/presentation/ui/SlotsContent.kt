package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.editor.EditorActionBar
import com.layzen.bdotools.feature.slots.presentation.ui.editor.EditorLegend
import com.layzen.bdotools.feature.slots.presentation.ui.editor.EditorStatus
import com.layzen.bdotools.feature.slots.presentation.ui.editor.SlotsCanvas
import com.layzen.bdotools.feature.slots.presentation.ui.editor.rememberCanvasViewport
import com.layzen.bdotools.feature.slots.presentation.ui.sidebar.SlotsFileActions
import com.layzen.bdotools.feature.slots.presentation.ui.sidebar.SlotsSidebar
import com.layzen.bdotools.uikit.components.ToolPage
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Страница после загрузки файла: настройки слева во всю высоту, справа панель действий
 * и холст редактора на всё оставшееся место.
 */
@Composable
internal fun SlotsContent(state: SlotsState, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val viewport = rememberCanvasViewport()
    ToolPage(
        sidebar = { SlotsSidebar(state, onEvent) },
        modifier = modifier,
        sidebarWidth = SlotsDimens.sidebarWidth,
        sidebarFooter = { SlotsFileActions(state, onEvent) },
    ) {
        Column(Modifier.fillMaxSize()) {
            EditorActionBar(state, onEvent)
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(AppTheme.dimens.spaceL),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
            ) {
                SlotsCanvas(state, viewport, onEvent, Modifier.weight(1f).fillMaxWidth())
                EditorLegend()
                EditorStatus(state)
            }
        }
    }
}
