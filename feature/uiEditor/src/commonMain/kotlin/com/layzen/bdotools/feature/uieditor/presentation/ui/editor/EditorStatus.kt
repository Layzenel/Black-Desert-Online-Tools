package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_off_screen
import com.layzen.bdotools.uikit.components.WarningText
import org.jetbrains.compose.resources.stringResource

/** Под холстом, всегда на виду: сколько элементов уехало за край экрана. */
@Composable
internal fun EditorStatus(state: SlotsState) {
    val screen = state.loaded?.game?.screen ?: return
    val offScreen = state.slots.count { id -> state.layout[id]?.let { !screen.fits(id, it) } == true }
    if (offScreen > 0) WarningText(stringResource(Res.string.slots_off_screen, offScreen))
}
