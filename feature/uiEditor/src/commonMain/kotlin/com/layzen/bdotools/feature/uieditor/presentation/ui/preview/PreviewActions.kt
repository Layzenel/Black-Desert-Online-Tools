package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.PreviewMode
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar.BackgroundDimSlider
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.menu_cancel
import com.layzen.bdotools.feature.uieditor.resources.menu_copy
import com.layzen.bdotools.feature.uieditor.resources.menu_export
import com.layzen.bdotools.feature.uieditor.resources.menu_hide
import com.layzen.bdotools.feature.uieditor.resources.menu_import
import com.layzen.bdotools.feature.uieditor.resources.menu_redo
import com.layzen.bdotools.feature.uieditor.resources.menu_select_all
import com.layzen.bdotools.feature.uieditor.resources.menu_select_none
import com.layzen.bdotools.feature.uieditor.resources.menu_show
import com.layzen.bdotools.feature.uieditor.resources.menu_undo
import com.layzen.bdotools.feature.uieditor.resources.slots_background_hide_preview
import com.layzen.bdotools.feature.uieditor.resources.slots_background_show_preview
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_import_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_background_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_confirm_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_copy
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_copy_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_exclude_all
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_exclude_all_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_export_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_include_all
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_include_all_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_redo_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_summary
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_summary_scale
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_undo_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_redo
import com.layzen.bdotools.feature.uieditor.resources.slots_undo
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Низ левой колонки превью: отмена и повтор отметок, «Выбрать всё» и «Снять всё», сколько выбрано
 * и сколько не влезет на экран, и последними — «Отмена» и главное действие. При наличии подложки — её переключатель и затенение.
 */
@Composable
internal fun PreviewActions(
    dialog: SlotsDialog.Preview,
    hasBackground: Boolean,
    backgroundShown: Boolean,
    backgroundDim: Int,
    onEvent: (SlotsEvent) -> Unit,
) {
    val all = dialog.layout.layout.positions.keys
    val allChats = dialog.layout.layout.chats.keys
    ButtonRow {
        ActionButton(ActionButtonState.Neutral(stringResource(Res.string.slots_undo), painterResource(Res.drawable.menu_undo), enabled = dialog.past.isNotEmpty(), onClick = { onEvent(SlotsEvent.PreviewUndo) }, tooltip = stringResource(Res.string.slots_preview_undo_tooltip)), Modifier.weight(1f))
        ActionButton(ActionButtonState.Neutral(stringResource(Res.string.slots_redo), painterResource(Res.drawable.menu_redo), enabled = dialog.future.isNotEmpty(), onClick = { onEvent(SlotsEvent.PreviewRedo) }, tooltip = stringResource(Res.string.slots_preview_redo_tooltip)), Modifier.weight(1f))
    }
    ButtonRow {
        ActionButton(
            ActionButtonState.Neutral(
                stringResource(Res.string.slots_preview_include_all),
                painterResource(Res.drawable.menu_select_all),
                enabled = dialog.included.size < all.size || dialog.includedChats.size < allChats.size,
                onClick = { onEvent(SlotsEvent.PreviewInclude(all, true, allChats)) },
                tooltip = stringResource(Res.string.slots_preview_include_all_tooltip),
            ),
            Modifier.weight(1f),
        )
        ActionButton(
            ActionButtonState.Neutral(
                stringResource(Res.string.slots_preview_exclude_all),
                painterResource(Res.drawable.menu_select_none),
                enabled = dialog.hasIncluded,
                onClick = { onEvent(SlotsEvent.PreviewInclude(all, false, allChats)) },
                tooltip = stringResource(Res.string.slots_preview_exclude_all_tooltip),
            ),
            Modifier.weight(1f),
        )
    }
    if (hasBackground) {
        ButtonRow {
            ActionButton(
                ActionButtonState.Neutral(
                    stringResource(if (backgroundShown) Res.string.slots_background_hide_preview else Res.string.slots_background_show_preview),
                    painterResource(if (backgroundShown) Res.drawable.menu_hide else Res.drawable.menu_show),
                    onClick = { onEvent(SlotsEvent.ToggleBackground) },
                    tooltip = stringResource(Res.string.slots_preview_background_tooltip),
                ),
                Modifier.weight(1f),
            )
        }
        if (backgroundShown) BackgroundDimSlider(backgroundDim, onEvent)
    }
    DescriptionText(summary(dialog))
    if (dialog.mode == PreviewMode.Export) {
        ButtonRow {
            ActionButton(
                ActionButtonState.Neutral(
                    stringResource(Res.string.slots_preview_copy),
                    painterResource(Res.drawable.menu_copy),
                    enabled = dialog.hasIncluded,
                    onClick = { onEvent(SlotsEvent.CopyPreview) },
                    tooltip = stringResource(Res.string.slots_preview_copy_tooltip),
                ),
                Modifier.weight(1f),
            )
        }
    }
    ButtonRow {
        ActionButton(ActionButtonState.Neutral(stringResource(Res.string.slots_cancel), painterResource(Res.drawable.menu_cancel), onClick = { onEvent(SlotsEvent.CloseDialog) }, tooltip = stringResource(Res.string.slots_cancel_tooltip)), Modifier.weight(1f))
        ActionButton(
            ActionButtonState.Positive(
                stringResource(if (dialog.mode == PreviewMode.Import) Res.string.slots_import_confirm else Res.string.slots_preview_export_confirm),
                painterResource(if (dialog.mode == PreviewMode.Import) Res.drawable.menu_import else Res.drawable.menu_export),
                enabled = dialog.hasIncluded,
                onClick = { onEvent(SlotsEvent.ConfirmPreview) },
                tooltip = stringResource(Res.string.slots_preview_confirm_tooltip),
            ),
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun ButtonRow(content: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS), content = content)
}

/** Сколько выбрано и сколько из выбранного не влезет на монитор превью. */
@Composable
private fun summary(dialog: SlotsDialog.Preview): String {
    val screen = dialog.screen
    val chosen = dialog.layout.layout.only(dialog.included, dialog.includedChats)
    val total = dialog.layout.layout.positions.size
    val offScreen = chosen.offScreenCount(screen)
    val scale = chosen.suggestedUiScale(screen)
    return if (scale == null) stringResource(Res.string.slots_preview_summary, dialog.included.size, total, offScreen)
    else stringResource(Res.string.slots_preview_summary_scale, dialog.included.size, total, offScreen, (scale * 100).roundToInt())
}
