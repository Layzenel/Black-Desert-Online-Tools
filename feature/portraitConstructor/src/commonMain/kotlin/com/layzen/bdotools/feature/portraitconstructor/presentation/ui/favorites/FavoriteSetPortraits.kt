package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.FavoritesDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid.PortraitGridLayout
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid.PortraitTile
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_delete_marked
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_portraits
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_rename
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Выбранный набор: поле названия (пустое — подпись датой, она же подсказка в поле), кнопки «Переименовать»
 * и «Удалить (N)» для отмеченных наборов, ниже — портреты набора.
 */
@Composable
internal fun FavoriteSetPortraits(state: FavoritesDialogState, onEvent: (PortraitsEvent) -> Unit, modifier: Modifier) {
    val set = state.selectedSet ?: return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            InputField(
                value = state.titleDraft,
                onValueChange = { onEvent(PortraitsEvent.FavoriteTitleChanged(it)) },
                modifier = Modifier.weight(1f),
                placeholder = set.dateLabel,
            )
            ActionButton(
                stringResource(Res.string.portraits_favorites_rename),
                onClick = { onEvent(PortraitsEvent.RenameFavorite) },
                enabled = state.titleChanged,
            )
            if (state.marked.isNotEmpty()) {
                ActionButton(
                    stringResource(Res.string.portraits_delete_marked, state.marked.size),
                    onClick = { onEvent(PortraitsEvent.RequestDeleteFavorites) },
                )
            }
        }
        SectionHeader(stringResource(Res.string.portraits_favorites_portraits, set.characterIds.size))
        PortraitGridLayout(
            items = state.previews.keys.toList() + (set.characterIds - state.previews.keys).sorted(),
            modifier = Modifier.heightIn(max = PortraitsDimens.restoreContentMaxHeight).verticalScroll(rememberScrollState()),
        ) { id ->
            PortraitTile(state.previews[id], contentDescription = id)
        }
    }
}
