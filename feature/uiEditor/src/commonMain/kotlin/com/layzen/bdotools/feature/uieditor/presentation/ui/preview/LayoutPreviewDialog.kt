package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.PreviewMode
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.feature.uieditor.presentation.ui.handleHistoryKeys
import com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar.QuickSlotSettings
import com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar.SplitHpSettings
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_export_title
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_import_title
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.ToolPage
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Превью импорта или экспорта почти на всё окно: слева выбор элементов и под ним все кнопки,
 * справа — как раскладка ляжет на экран. При экспорте над элементами — монитор для просмотра (по умолчанию
 * экран игры [gameScreen]). Импортируются или экспортируются только отмеченные элементы. [background] — подложка
 * из редактора, [backgroundShown] — включена ли она, [backgroundDim] — её затенение в процентах; переключатель стоит среди кнопок слева.
 * Ctrl+Z отменяет последнюю отметку, Ctrl+Shift+Z и Ctrl+Y повторяют её.
 */
@Composable
internal fun LayoutPreviewDialog(
    dialog: SlotsDialog.Preview,
    gameScreen: ScreenSpace,
    background: ImageBitmap?,
    backgroundShown: Boolean,
    backgroundDim: Int,
    onEvent: (SlotsEvent) -> Unit,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(focus) { focus.requestFocus() }
    ModalWindow(
        title = stringResource(if (dialog.mode == PreviewMode.Import) Res.string.slots_preview_import_title else Res.string.slots_preview_export_title),
        onDismissRequest = { onEvent(SlotsEvent.CloseDialog) },
        modifier = Modifier.fillMaxWidth(SlotsDimens.PREVIEW_WIDTH_SHARE).fillMaxHeight(SlotsDimens.PREVIEW_HEIGHT_SHARE),
    ) {
        ToolPage(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focus)
                .focusable()
                .onPreviewKeyEvent { event ->
                    event.handleHistoryKeys(onUndo = { onEvent(SlotsEvent.PreviewUndo) }, onRedo = { onEvent(SlotsEvent.PreviewRedo) })
                },
            sidebarWidth = SlotsDimens.previewSidebarWidth,
            sidebar = {
                if (dialog.mode == PreviewMode.Export) {
                    PreviewScreenSettings(dialog.screen, gameScreen, onEvent)
                    SectionDivider()
                }
                val layout = dialog.layout.layout
                QuickSlotSettings(layout.customQuickSlots) { onEvent(SlotsEvent.PreviewCustomQuickSlots(it)) }
                SplitHpSettings(layout.splitHp) { onEvent(SlotsEvent.PreviewSplitHp(it)) }
                SectionDivider()
                PreviewElementList(layout.positions.keys.filter { layout.isActive(it) }.toSet(), dialog.included, onEvent)
                PreviewChatList(layout.chats.keys, dialog.includedChats, onEvent)
            },
            sidebarFooter = { PreviewActions(dialog, hasBackground = background != null, backgroundShown, backgroundDim, onEvent) },
        ) {
            Column(Modifier.fillMaxSize().padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                PreviewCanvas(
                    dialog.layout.layout,
                    dialog.included,
                    dialog.includedChats,
                    dialog.screen,
                    background.takeIf { backgroundShown },
                    backgroundDim,
                    onEvent,
                    Modifier.weight(1f).fillMaxWidth(),
                )
                DescriptionText(stringResource(Res.string.slots_preview_hint))
            }
        }
    }
}
