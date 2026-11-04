package com.layzen.bdotools.feature.slots.presentation.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.PreviewMode
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_cancel
import com.layzen.bdotools.feature.slots.resources.slots_import_confirm
import com.layzen.bdotools.feature.slots.resources.slots_preview_export_confirm
import com.layzen.bdotools.feature.slots.resources.slots_preview_export_title
import com.layzen.bdotools.feature.slots.resources.slots_preview_import_title
import com.layzen.bdotools.feature.slots.resources.slots_preview_summary
import com.layzen.bdotools.feature.slots.resources.slots_preview_summary_scale
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Превью импорта или экспорта: слева монитор для просмотра и выбор элементов, справа — как раскладка ляжет
 * на этот монитор. Импортируются или экспортируются только отмеченные элементы.
 */
@Composable
internal fun LayoutPreviewDialog(dialog: SlotsDialog.Preview, gameScreen: ScreenSpace, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    val import = dialog.mode == PreviewMode.Import
    ModalWindow(
        title = stringResource(if (import) Res.string.slots_preview_import_title else Res.string.slots_preview_export_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.previewWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(
                stringResource(if (import) Res.string.slots_import_confirm else Res.string.slots_preview_export_confirm),
                onClick = { onEvent(SlotsEvent.ConfirmPreview) },
                enabled = dialog.included.isNotEmpty(),
            )
        },
    ) {
        Row(Modifier.height(SlotsDimens.previewHeight)) {
            PreviewSidebar(dialog, gameScreen, onEvent)
            Column(
                Modifier.weight(1f).fillMaxHeight().padding(AppTheme.dimens.spaceL),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
            ) {
                PreviewCanvas(dialog.layout.layout, dialog.included, dialog.screen, onEvent, Modifier.weight(1f).fillMaxWidth())
                DescriptionText(summary(dialog))
            }
        }
    }
}

@Composable
private fun PreviewSidebar(dialog: SlotsDialog.Preview, gameScreen: ScreenSpace, onEvent: (SlotsEvent) -> Unit) {
    val scroll = rememberScrollState()
    Column(
        Modifier
            .width(SlotsDimens.sidebarWidth)
            .fillMaxHeight()
            .background(AppTheme.colors.backgroundSurface)
            .verticalScrollbar(scroll)
            .verticalScroll(scroll)
            .padding(AppTheme.dimens.spaceL),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
    ) {
        PreviewScreenSettings(dialog.screen, gameScreen, onEvent)
        SectionDivider()
        PreviewElementList(dialog.layout.layout.positions.keys, dialog.included, onEvent)
    }
}

/** Сколько выбрано и сколько из выбранного не влезет на монитор превью. */
@Composable
private fun summary(dialog: SlotsDialog.Preview): String {
    val chosen = dialog.layout.layout.only(dialog.included)
    val total = dialog.layout.layout.positions.size
    val offScreen = chosen.offScreenCount(dialog.screen)
    val scale = chosen.suggestedUiScale(dialog.screen)
    return if (scale == null) stringResource(Res.string.slots_preview_summary, dialog.included.size, total, offScreen)
    else stringResource(Res.string.slots_preview_summary_scale, dialog.included.size, total, offScreen, (scale * 100).roundToInt())
}
