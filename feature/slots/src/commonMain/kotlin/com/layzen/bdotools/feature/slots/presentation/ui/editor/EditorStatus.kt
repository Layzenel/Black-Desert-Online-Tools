package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_off_screen
import com.layzen.bdotools.feature.slots.resources.slots_screen
import com.layzen.bdotools.feature.slots.resources.slots_unsaved
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.WarningText
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Под холстом: размер экрана игры, слоты за краем и несохранённые изменения. */
@Composable
internal fun EditorStatus(state: SlotsState) {
    val screen = state.loaded?.game?.screen ?: return
    DescriptionText(stringResource(Res.string.slots_screen, screen.width.roundToInt(), screen.height.roundToInt()))
    val offScreen = state.visibleSlots.count { id -> state.layout[id]?.let { !screen.fits(id, it) } == true }
    if (offScreen > 0) WarningText(stringResource(Res.string.slots_off_screen, offScreen))
    if (state.hasChanges) DescriptionText(stringResource(Res.string.slots_unsaved))
}
