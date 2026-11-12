package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_frame_transparency
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.stringResource

/** Прозрачность рамки слотов перезарядки с навыком, [percent] %: 100 — видны только иконки, как в игре. */
@Composable
internal fun SkillFrameTransparency(percent: Int, onEvent: (SlotsEvent) -> Unit) {
    DescriptionText(stringResource(Res.string.slots_frame_transparency))
    ValueSlider(percent, { onEvent(SlotsEvent.SetSkillFrameTransparency(it)) }, range = SlotsState.SKILL_FRAME_TRANSPARENCY_RANGE)
}
