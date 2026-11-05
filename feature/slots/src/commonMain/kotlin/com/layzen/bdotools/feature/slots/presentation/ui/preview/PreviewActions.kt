package com.layzen.bdotools.feature.slots.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.PreviewMode
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.menu_cancel
import com.layzen.bdotools.feature.slots.resources.menu_export
import com.layzen.bdotools.feature.slots.resources.menu_hide
import com.layzen.bdotools.feature.slots.resources.menu_import
import com.layzen.bdotools.feature.slots.resources.menu_redo
import com.layzen.bdotools.feature.slots.resources.menu_select_all
import com.layzen.bdotools.feature.slots.resources.menu_select_none
import com.layzen.bdotools.feature.slots.resources.menu_show
import com.layzen.bdotools.feature.slots.resources.menu_undo
import com.layzen.bdotools.feature.slots.resources.slots_background_hide_preview
import com.layzen.bdotools.feature.slots.resources.slots_background_show_preview
import com.layzen.bdotools.feature.slots.resources.slots_cancel
import com.layzen.bdotools.feature.slots.resources.slots_import_confirm
import com.layzen.bdotools.feature.slots.resources.slots_preview_exclude_all
import com.layzen.bdotools.feature.slots.resources.slots_preview_export_confirm
import com.layzen.bdotools.feature.slots.resources.slots_preview_include_all
import com.layzen.bdotools.feature.slots.resources.slots_preview_summary
import com.layzen.bdotools.feature.slots.resources.slots_preview_summary_scale
import com.layzen.bdotools.feature.slots.resources.slots_redo
import com.layzen.bdotools.feature.slots.resources.slots_undo
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Низ левой колонки превью: отмена и повтор отметок, «Выбрать всё» и «Снять всё», сколько выбрано
 * и сколько не влезет на экран, и последними — «Отмена» и главное действие. При наличии подложки — её переключатель.
 */
@Composable
internal fun PreviewActions(dialog: SlotsDialog.Preview, hasBackground: Boolean, backgroundShown: Boolean, onEvent: (SlotsEvent) -> Unit) {
    val all = dialog.layout.layout.positions.keys
    ButtonRow {
        MenuAction(stringResource(Res.string.slots_undo), { onEvent(SlotsEvent.PreviewUndo) }, Modifier.weight(1f), painterResource(Res.drawable.menu_undo), enabled = dialog.past.isNotEmpty())
        MenuAction(stringResource(Res.string.slots_redo), { onEvent(SlotsEvent.PreviewRedo) }, Modifier.weight(1f), painterResource(Res.drawable.menu_redo), enabled = dialog.future.isNotEmpty())
    }
    ButtonRow {
        MenuAction(
            stringResource(Res.string.slots_preview_include_all),
            { onEvent(SlotsEvent.PreviewInclude(all, true)) },
            Modifier.weight(1f),
            painterResource(Res.drawable.menu_select_all),
            enabled = dialog.included.size < all.size,
        )
        MenuAction(
            stringResource(Res.string.slots_preview_exclude_all),
            { onEvent(SlotsEvent.PreviewInclude(all, false)) },
            Modifier.weight(1f),
            painterResource(Res.drawable.menu_select_none),
            enabled = dialog.included.isNotEmpty(),
        )
    }
    if (hasBackground) {
        ButtonRow {
            MenuAction(
                stringResource(if (backgroundShown) Res.string.slots_background_hide_preview else Res.string.slots_background_show_preview),
                { onEvent(SlotsEvent.ToggleBackground) },
                Modifier.weight(1f),
                painterResource(if (backgroundShown) Res.drawable.menu_hide else Res.drawable.menu_show),
            )
        }
    }
    DescriptionText(summary(dialog))
    ButtonRow {
        MenuAction(stringResource(Res.string.slots_cancel), { onEvent(SlotsEvent.CloseDialog) }, Modifier.weight(1f), painterResource(Res.drawable.menu_cancel))
        MenuAction(
            stringResource(if (dialog.mode == PreviewMode.Import) Res.string.slots_import_confirm else Res.string.slots_preview_export_confirm),
            { onEvent(SlotsEvent.ConfirmPreview) },
            Modifier.weight(1f),
            painterResource(if (dialog.mode == PreviewMode.Import) Res.drawable.menu_import else Res.drawable.menu_export),
            enabled = dialog.included.isNotEmpty(),
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
    val chosen = dialog.layout.layout.only(dialog.included)
    val total = dialog.layout.layout.positions.size
    val offScreen = chosen.offScreenCount(screen)
    val scale = chosen.suggestedUiScale(screen)
    return if (scale == null) stringResource(Res.string.slots_preview_summary, dialog.included.size, total, offScreen)
    else stringResource(Res.string.slots_preview_summary_scale, dialog.included.size, total, offScreen, (scale * 100).roundToInt())
}
