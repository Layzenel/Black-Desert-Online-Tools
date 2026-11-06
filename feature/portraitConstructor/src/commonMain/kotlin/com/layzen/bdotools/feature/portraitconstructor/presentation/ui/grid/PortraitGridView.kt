package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import com.layzen.bdotools.uikit.components.HoverActionsBox

/**
 * Портреты на странице. При наведении на портрет поверх него появляются действия [PortraitMenu];
 * сам портрет на клик не реагирует. Пока портреты читаются, на месте непрочитанных — скелетоны, а действий нет.
 * В режиме изменения порядка действий тоже нет: портреты затемнены, клик выбирает портрет, выбранный подсвечен.
 * Два обменянных портрета вспыхивают золотом с искрами, как в окне выбора персонажа игры.
 */
@Composable
internal fun PortraitGridView(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val reorder = state.reorder
    val loading = state.loading != null
    PortraitGridLayout(state.portraits) { portrait ->
        val id = portrait.characterId
        val swapPhase = state.swapAnimation?.takeIf { it.involves(id) }?.phase
        when {
            loading -> PortraitTile(portrait.image, contentDescription = id)
            reorder != null -> {
                // Режим после обмена не выключается: вспышка идёт прямо в нём, обменянные портреты не затемнены
                val picked = id == reorder.first
                PortraitTile(
                    portrait.image,
                    modifier = Modifier.swapGlow(swapPhase),
                    contentDescription = id,
                    selected = picked,
                    dimmed = !picked && swapPhase == null,
                    onClick = { onEvent(PortraitsEvent.PickReorderPortrait(id)) },
                )
            }
            else -> {
                HoverActionsBox(
                    actions = { PortraitMenu(id, hasBackups = state.hasBackups(id), onEvent = onEvent) },
                ) {
                    PortraitTile(portrait.image, modifier = Modifier.swapGlow(swapPhase), contentDescription = id)
                }
            }
        }
    }
}
