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
import com.layzen.bdotools.feature.slots.resources.slots_cancel
import com.layzen.bdotools.feature.slots.resources.slots_import_confirm
import com.layzen.bdotools.feature.slots.resources.slots_preview_exclude_all
import com.layzen.bdotools.feature.slots.resources.slots_preview_export_confirm
import com.layzen.bdotools.feature.slots.resources.slots_preview_include_all
import com.layzen.bdotools.feature.slots.resources.slots_preview_summary
import com.layzen.bdotools.feature.slots.resources.slots_preview_summary_scale
import com.layzen.bdotools.feature.slots.resources.slots_redo
import com.layzen.bdotools.feature.slots.resources.slots_undo
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Низ левой колонки превью: отмена и повтор отметок, «Выбрать всё» и «Снять всё», сколько выбрано
 * и сколько не влезет на экран, и последними — «Отмена» и главное действие.
 */
@Composable
internal fun PreviewActions(dialog: SlotsDialog.Preview, onEvent: (SlotsEvent) -> Unit) {
    val all = dialog.layout.layout.positions.keys
    ButtonRow {
        ActionButton(stringResource(Res.string.slots_undo), { onEvent(SlotsEvent.PreviewUndo) }, Modifier.weight(1f), enabled = dialog.past.isNotEmpty())
        ActionButton(stringResource(Res.string.slots_redo), { onEvent(SlotsEvent.PreviewRedo) }, Modifier.weight(1f), enabled = dialog.future.isNotEmpty())
    }
    ButtonRow {
        ActionButton(
            stringResource(Res.string.slots_preview_include_all),
            { onEvent(SlotsEvent.PreviewInclude(all, true)) },
            Modifier.weight(1f),
            enabled = dialog.included.size < all.size,
        )
        ActionButton(
            stringResource(Res.string.slots_preview_exclude_all),
            { onEvent(SlotsEvent.PreviewInclude(all, false)) },
            Modifier.weight(1f),
            enabled = dialog.included.isNotEmpty(),
        )
    }
    DescriptionText(summary(dialog))
    ButtonRow {
        ActionButton(stringResource(Res.string.slots_cancel), { onEvent(SlotsEvent.CloseDialog) }, Modifier.weight(1f))
        ActionButton(
            stringResource(if (dialog.mode == PreviewMode.Import) Res.string.slots_import_confirm else Res.string.slots_preview_export_confirm),
            { onEvent(SlotsEvent.ConfirmPreview) },
            Modifier.weight(1f),
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
