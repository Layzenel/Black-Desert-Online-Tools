package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.FavoritesDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.ConfirmationDialog
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_delete_confirm
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_apply
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_close
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_delete_text
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_delete_title
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_empty
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_loading
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_replace
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_replace_confirm
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_replace_text
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_replace_title
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_save
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_sets
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Избранное — наборы всех портретов в `Favorite FaceTexture`: слева список наборов (галочка отмечает набор
 * для удаления), справа название и портреты выбранного. Сохраняются и применяются только все портреты сразу.
 */
@Composable
internal fun FavoritesDialog(state: FavoritesDialogState, onEvent: (PortraitsEvent) -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.portraits_favorites_title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseFavorites) },
        modifier = Modifier.width(PortraitsDimens.restoreDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_favorites_close), onClick = { onEvent(PortraitsEvent.CloseFavorites) })
            ActionButton(stringResource(Res.string.portraits_favorites_save), onClick = { onEvent(PortraitsEvent.SaveFavorites) })
            ActionButton(
                stringResource(Res.string.portraits_favorites_replace),
                onClick = { onEvent(PortraitsEvent.RequestReplaceFavorite) },
                enabled = state.selectedSet != null,
            )
            ActionButton(
                stringResource(Res.string.portraits_favorites_apply),
                onClick = { onEvent(PortraitsEvent.ApplyFavorite) },
                enabled = state.selectedSet != null,
            )
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            DescriptionText(stringResource(Res.string.portraits_favorites_description))
            val sets = state.sets
            when {
                sets == null -> LoadingRow(stringResource(Res.string.portraits_favorites_loading))
                sets.isEmpty() -> DescriptionText(stringResource(Res.string.portraits_favorites_empty))
                else -> Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL)) {
                    FavoriteSetList(state, onEvent)
                    FavoriteSetPortraits(state, onEvent, Modifier.weight(1f))
                }
            }
        }
    }

    if (state.confirmDelete) {
        val marked = state.sets.orEmpty().filter { it.name in state.marked }
        ConfirmationDialog(
            title = stringResource(Res.string.portraits_favorites_delete_title),
            warning = stringResource(Res.string.portraits_favorites_delete_text, marked.size),
            items = marked.map { it.label },
            confirm = stringResource(Res.string.portraits_delete_confirm),
            onCancel = { onEvent(PortraitsEvent.CancelDeleteFavorites) },
            onConfirm = { onEvent(PortraitsEvent.ConfirmDeleteFavorites) },
        )
    }
    val selected = state.selectedSet
    if (state.confirmReplace && selected != null) {
        ConfirmationDialog(
            title = stringResource(Res.string.portraits_favorites_replace_title),
            warning = stringResource(Res.string.portraits_favorites_replace_text),
            items = listOf(selected.label),
            confirm = stringResource(Res.string.portraits_favorites_replace_confirm),
            onCancel = { onEvent(PortraitsEvent.CancelReplaceFavorite) },
            onConfirm = { onEvent(PortraitsEvent.ConfirmReplaceFavorite) },
        )
    }
}

@Composable
private fun FavoriteSetList(state: FavoritesDialogState, onEvent: (PortraitsEvent) -> Unit) {
    Column(
        Modifier
            .width(PortraitsDimens.restoreListWidth)
            .heightIn(max = PortraitsDimens.restoreContentMaxHeight)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionHeader(stringResource(Res.string.portraits_favorites_sets), Modifier.padding(bottom = AppTheme.dimens.spaceS))
        state.sets.orEmpty().forEach { set ->
            // галочка внутри строки отмечает набор для удаления, клик по остальной строке открывает его
            MenuItem(
                text = set.label,
                hotkey = set.characterIds.size.toString(),
                selected = set.name == state.selected,
                onClick = { onEvent(PortraitsEvent.SelectFavorite(set.name)) },
                icon = {
                    CheckBox(
                        checked = set.name in state.marked,
                        onCheckedChange = { onEvent(PortraitsEvent.ToggleFavoriteMark(set.name)) },
                    )
                },
            )
        }
    }
}
