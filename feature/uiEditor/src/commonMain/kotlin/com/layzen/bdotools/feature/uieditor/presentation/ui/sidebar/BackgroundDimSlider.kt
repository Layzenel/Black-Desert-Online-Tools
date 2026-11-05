package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_background_dim
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.stringResource

/** Подпись и слайдер затенения подложки; значение общее для редактора и превью. */
@Composable
internal fun BackgroundDimSlider(dimPercent: Int, onEvent: (SlotsEvent) -> Unit) {
    DescriptionText(stringResource(Res.string.slots_background_dim))
    ValueSlider(dimPercent, { onEvent(SlotsEvent.SetBackgroundDim(it)) }, range = SlotsState.BACKGROUND_DIM_RANGE)
}
