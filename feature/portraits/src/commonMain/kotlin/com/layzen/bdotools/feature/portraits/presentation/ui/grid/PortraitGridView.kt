package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.state.SwapAnimation
import com.layzen.bdotools.uikit.components.HoverActionsBox

/**
 * Портреты на странице. При наведении на портрет поверх него появляются действия [PortraitMenu];
 * сам портрет на клик не реагирует. Пока портреты читаются, на месте непрочитанных — скелетоны, а действий нет.
 * В режиме изменения порядка действий тоже нет: портреты затемнены, клик выбирает портрет, выбранный подсвечен.
 * Два обменянных портрета гаснут и проявляются заново.
 */
@Composable
internal fun PortraitGridView(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val reorder = state.reorder
    val loading = state.loading != null
    PortraitGridLayout(state.portraits) { portrait ->
        val id = portrait.characterId
        when {
            loading -> PortraitTile(portrait.image, contentDescription = id)
            reorder != null -> {
                val picked = id == reorder.first
                PortraitTile(
                    portrait.image,
                    contentDescription = id,
                    selected = picked,
                    dimmed = !picked,
                    onClick = { onEvent(PortraitsEvent.PickReorderPortrait(id)) },
                )
            }
            else -> {
                val swapPhase = state.swapAnimation?.takeIf { it.involves(id) }?.phase
                HoverActionsBox(
                    actions = { PortraitMenu(id, hasBackups = state.hasBackups(id), onEvent = onEvent) },
                ) {
                    PortraitTile(
                        portrait.image,
                        modifier = Modifier.swapAnimation(swapPhase),
                        contentDescription = id,
                        selected = swapPhase == SwapAnimation.Phase.In,
                    )
                }
            }
        }
    }
}
