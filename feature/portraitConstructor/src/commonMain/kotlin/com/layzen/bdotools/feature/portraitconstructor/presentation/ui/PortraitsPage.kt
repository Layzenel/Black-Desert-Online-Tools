package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_grid_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_grid_title
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_description
import com.layzen.bdotools.uikit.components.ContentFrame
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource

/**
 * Страница портретов после доступа к папке, по центру окна на подложке [ContentFrame]: сколько персонажей
 * и подсказка, сетка портретов и строка действий под ней. Esc выключает режим изменения порядка;
 * на время режима фокус переходит на страницу, чтобы Esc сработал сразу.
 */
@Composable
internal fun PortraitsPage(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val focus = remember { FocusRequester() }
    val reordering = state.reorder != null
    LaunchedEffect(reordering) { if (reordering) focus.requestFocus() }
    ContentFrame(
        Modifier
            .widthIn(max = PortraitsDimens.gridMaxWidth)
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent { event ->
                val cancel = reordering && event.key == Key.Escape
                if (cancel && event.type == KeyEventType.KeyDown) onEvent(PortraitsEvent.CancelReorder)
                cancel
            },
    ) {
        SectionHeader(
            stringResource(Res.string.portraits_grid_title, state.portraits.size),
            description = stringResource(if (reordering) Res.string.portraits_reorder_description else Res.string.portraits_grid_description),
        )
        PortraitsContent(state, onEvent)
        PortraitsActions(state, onEvent)
    }
}
