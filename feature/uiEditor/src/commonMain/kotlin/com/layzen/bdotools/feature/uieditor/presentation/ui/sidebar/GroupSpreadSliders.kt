package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.domain.arrangement.groupSpread
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_spread_description
import com.layzen.bdotools.feature.uieditor.resources.slots_spread_horizontal
import com.layzen.bdotools.feature.uieditor.resources.slots_spread_vertical
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.stringResource

/**
 * Слайдеры «Отдаление» по горизонтали и по вертикали, когда среди выбранного несколько групп: каждый разносит группы
 * от центра между ними. Слайдер оси, по которой группы стоят на одной линии, не показывается. Справа предел —
 * половина экрана, слева — расстояние, на котором группы сойдутся вплотную.
 */
@Composable
internal fun GroupSpreadSliders(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val screen = state.loaded?.game?.screen ?: return
    val axes = listOf(true, false).mapNotNull { horizontal ->
        state.layout.groupSpread(state.selection, state.selectedChats, horizontal)?.let { horizontal to it }
    }
    if (axes.isEmpty()) return
    DescriptionText(stringResource(Res.string.slots_spread_description))
    axes.forEach { (horizontal, spread) ->
        DescriptionText(stringResource(if (horizontal) Res.string.slots_spread_horizontal else Res.string.slots_spread_vertical))
        val halfScreen = ((if (horizontal) screen.width else screen.height) / 2).toInt()
        ValueSlider(
            spread.value,
            { onEvent(SlotsEvent.SpreadGroups(horizontal, it)) },
            range = spread.min..maxOf(halfScreen, spread.value, spread.min),
            onValueChangeFinished = { onEvent(SlotsEvent.SpreadGroupsEnd) },
        )
    }
}
