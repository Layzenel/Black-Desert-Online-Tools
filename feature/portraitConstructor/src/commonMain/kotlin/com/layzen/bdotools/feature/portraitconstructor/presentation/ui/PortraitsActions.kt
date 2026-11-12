package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.layzen.bdotools.feature.portraitconstructor.presentation.drawable
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_backups
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_favorites
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_notes
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_reorder
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_upload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_cancel
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_cancel_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_hotkey
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_upload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_upload_hint
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Строка действий под сеткой портретов, главное «Загрузить» — последним справа. Папка и «Обновить» —
 * значками у заголовка ([PortraitsFolderTools]): так пяти подписям хватает места без многоточия.
 * Пока портреты грузятся, действия неактивны. В режиме изменения порядка его пункт подсвечен и выключает режим.
 * Подписи короткие, поэтому у каждого действия при наведении — подсказка, что оно делает.
 */
@Composable
internal fun PortraitsActions(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val idle = state.loading == null
    val hasPortraits = idle && state.portraits.isNotEmpty()
    val reordering = state.reorder != null
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        HintedAction(
            hint = stringResource(if (reordering) Res.string.portraits_reorder_cancel_hint else Res.string.portraits_reorder_hint),
            text = stringResource(if (reordering) Res.string.portraits_reorder_cancel else Res.string.portraits_reorder_button),
            icon = painterResource(Res.drawable.menu_reorder),
            enabled = reordering || idle && state.portraits.size >= ReorderState.SWAP_SIZE,
            hotkey = if (reordering) stringResource(Res.string.portraits_reorder_hotkey) else null,
            selected = reordering,
            onClick = { onEvent(if (reordering) PortraitsEvent.CancelReorder else PortraitsEvent.StartReorder) },
        )
        HintedAction(
            hint = stringResource(Res.string.portraits_restore_hint),
            text = stringResource(Res.string.portraits_restore_button),
            icon = painterResource(Res.drawable.menu_backups),
            enabled = idle && state.backups.isNotEmpty(),
            onClick = { onEvent(PortraitsEvent.OpenRestore(ReplaceTarget.All)) },
        )
        HintedAction(
            hint = stringResource(Res.string.portraits_favorites_hint),
            text = stringResource(Res.string.portraits_favorites_button),
            icon = painterResource(Res.drawable.menu_favorites),
            enabled = hasPortraits,
            onClick = { onEvent(PortraitsEvent.OpenFavorites) },
        )
        HintedAction(
            hint = stringResource(Res.string.portraits_notes_hint),
            text = stringResource(Res.string.portraits_notes_button),
            icon = painterResource(Res.drawable.menu_notes),
            enabled = hasPortraits && state.accountId != null,
            onClick = { onEvent(PortraitsEvent.OpenNotesSync) },
        )
        HintedAction(
            hint = stringResource(Res.string.portraits_upload_hint),
            text = stringResource(Res.string.portraits_upload),
            icon = painterResource(Res.drawable.menu_upload),
            enabled = hasPortraits,
            onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.All)) },
        )
    }
}

/** Пункт строки действий равной ширины с подсказкой [hint] при наведении. */
@Composable
private fun RowScope.HintedAction(
    hint: String,
    text: String,
    icon: Painter,
    enabled: Boolean,
    onClick: () -> Unit,
    hotkey: String? = null,
    selected: Boolean = false,
) {
    // TooltipBox не передаёт weight в Row, поэтому вес — у обёртки, а подсказка внутри неё
    Box(Modifier.weight(1f)) {
        HoverTooltip(hint) {
            MenuAction(text, onClick, Modifier.fillMaxWidth(), icon = icon, hotkey = hotkey, selected = selected, enabled = enabled)
        }
    }
}
