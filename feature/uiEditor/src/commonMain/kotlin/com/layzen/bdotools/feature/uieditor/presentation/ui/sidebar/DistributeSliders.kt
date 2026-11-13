package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.domain.arrangement.distributionGap
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_distribute_horizontal
import com.layzen.bdotools.feature.uieditor.resources.slots_distribute_vertical
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.stringResource

/**
 * «Распределить по горизонтали» и «по вертикали» для выбранной группы элементов: слайдер — промежуток между рамками,
 * UI-px, элементы расставляются друг за другом прямо по ходу жеста. То же, что пункты меню правого клика.
 * Значение — текущий промежуток самой группы (отрицательный — колонки заходят друг на друга); по оси, где она стоит
 * в одну колонку (ряд), слайдера нет.
 */
@Composable
internal fun DistributeSliders(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    listOf(true, false).forEach { horizontal ->
        val gap = state.layout.distributionGap(state.selection, horizontal) ?: return@forEach
        DescriptionText(stringResource(if (horizontal) Res.string.slots_distribute_horizontal else Res.string.slots_distribute_vertical))
        ValueSlider(
            gap.coerceIn(state.distributeGapRange(horizontal)),
            { onEvent(SlotsEvent.SetDistributeGap(it, horizontal)) },
            range = state.distributeGapRange(horizontal),
            onValueChangeFinished = { onEvent(SlotsEvent.DistributeGapFinished) },
        )
    }
}
