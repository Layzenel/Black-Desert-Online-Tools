package com.layzen.bdotools.feature.comboassist.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.ComboViewModel
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboMode
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.CellPanel
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.ComboCanvas
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.ComboParts
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.OptionalSkillsEditor
import com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar.ComboSidebarContent
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlaysettings.OverlaySettingsPanel
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_delete
import com.layzen.bdotools.feature.comboassist.resources.combo_export
import com.layzen.bdotools.feature.comboassist.resources.menu_delete
import com.layzen.bdotools.feature.comboassist.resources.menu_export
import com.layzen.bdotools.feature.comboassist.resources.menu_import
import com.layzen.bdotools.feature.comboassist.resources.menu_save
import com.layzen.bdotools.feature.comboassist.resources.combo_import
import com.layzen.bdotools.feature.comboassist.resources.combo_import_failed
import com.layzen.bdotools.feature.comboassist.resources.combo_empty_hint
import com.layzen.bdotools.feature.comboassist.resources.combo_mode_editor
import com.layzen.bdotools.feature.comboassist.resources.combo_mode_overlay
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_open
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_remove
import com.layzen.bdotools.feature.comboassist.resources.menu_info
import com.layzen.bdotools.feature.comboassist.resources.combo_save
import com.layzen.bdotools.feature.comboassist.resources.combo_save_dirty
import com.layzen.bdotools.feature.comboassist.resources.combo_untitled
import com.layzen.bdotools.feature.comboassist.resources.combo_warning_chain
import com.layzen.bdotools.feature.comboassist.resources.combo_warning_empty
import com.layzen.bdotools.uikit.components.ActionBar
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.ToolPage
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Вкладка комбо: ViewModel из Koin. */
@Composable
fun ComboRoute(viewModel: ComboViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ComboScreen(state, viewModel.canWatchGame, viewModel::onEvent, viewModel::details)
}

/** Подгружает строки вкладки заранее, чтобы в браузере кнопки не были на миг пустыми. */
suspend fun preloadComboStrings() {
    getString(Res.string.combo_save)
}

/** Редактор комбо и настройка оверлея (настройка — только в настольной версии, [canWatchGame]). Окна поверх игры — в [com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.comboOverlayContribution]. */
@Composable
fun ComboScreen(state: ComboState, canWatchGame: Boolean, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    // Ctrl+Z ловит сама страница: поле ввода отменяет свой текст само и до неё клавишу не пускает
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    ToolPage(
        modifier = Modifier.fillMaxSize().focusRequester(focus).onKeyEvent { event ->
            val undo = event.type == KeyEventType.KeyDown && (event.isCtrlPressed || event.isMetaPressed) && !event.isShiftPressed && event.key == Key.Z
            if (undo) onEvent(ComboEvent.Undo)
            undo
        }.focusable(),
        sidebarWidth = ComboDimens.sidebarWidth,
        sidebar = { ComboSidebarContent(state, onEvent, loadDetails) },
        sidebarFooter = {
            if (canWatchGame) MenuAction(stringResource(Res.string.combo_notice_open), { onEvent(ComboEvent.ShowTrackerNotice) }, Modifier.fillMaxWidth(), icon = painterResource(Res.drawable.menu_info))
            if (state.importFailed) WarningText(stringResource(Res.string.combo_import_failed))
            MenuAction(stringResource(Res.string.combo_import), { onEvent(ComboEvent.ImportCombos) }, Modifier.fillMaxWidth(), icon = painterResource(Res.drawable.menu_import))
            MenuAction(stringResource(Res.string.combo_export), { onEvent(ComboEvent.ExportCombo) }, Modifier.fillMaxWidth(), icon = painterResource(Res.drawable.menu_export), enabled = state.combo != null)
            if (state.combo?.rage != null) {
                MenuAction(stringResource(Res.string.combo_rage_remove), { onEvent(ComboEvent.RemoveRage) }, Modifier.fillMaxWidth(), icon = painterResource(Res.drawable.menu_delete))
            }
            MenuAction(stringResource(Res.string.combo_delete), { onEvent(ComboEvent.AskDelete) }, Modifier.fillMaxWidth(), icon = painterResource(Res.drawable.menu_delete), enabled = state.combo != null)
            MenuAction(stringResource(if (state.dirty) Res.string.combo_save_dirty else Res.string.combo_save), { onEvent(ComboEvent.Save) }, Modifier.fillMaxWidth(), icon = painterResource(Res.drawable.menu_save), enabled = state.combo != null)
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            // настройка оверлея есть только в настольной версии: на сайте остаётся один редактор и переключать нечего
            if (canWatchGame) ActionBar {
                OptionGroup(ComboMode.entries, state.mode, { onEvent(ComboEvent.SetMode(it)) }, { stringResource(if (it == ComboMode.Editor) Res.string.combo_mode_editor else Res.string.combo_mode_overlay) })
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppTheme.dimens.spaceL),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL),
            ) {
                if (state.mode == ComboMode.Overlay && canWatchGame) OverlaySettingsPanel(state, onEvent, loadDetails)
                else EditorContent(state, onEvent, loadDetails)
            }
        }
    }
    if (state.trackerNotice) TrackerNoticeDialog { onEvent(ComboEvent.CloseTrackerNotice) }
    val deleted = state.combo
    if (state.deleteAsked && deleted != null) {
        val name = deleted.name.ifBlank { stringResource(Res.string.combo_untitled) }
        DeleteComboDialog(name, onCancel = { onEvent(ComboEvent.CancelDelete) }, onConfirm = { onEvent(ComboEvent.Delete) })
    }
}

@Composable
private fun EditorContent(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.edited ?: return
    ComboParts(state, onEvent)
    DescriptionText(stringResource(Res.string.combo_empty_hint))
    // Esc снимает выбор ячейки: холст берёт фокус, когда ячейку выбрали, иначе клавиши до него не дойдут
    val focus = remember { FocusRequester() }
    LaunchedEffect(state.selection) { if (state.selection != null) focus.requestFocus() }
    Column(
        Modifier.horizontalScroll(rememberScrollState()).focusRequester(focus).focusable()
            .onPreviewKeyEvent { event ->
                val escape = event.type == KeyEventType.KeyDown && event.key == Key.Escape && state.selection != null
                if (escape) onEvent(ComboEvent.ClearSelection)
                escape
            },
    ) { ComboCanvas(state, onEvent, loadDetails) }
    CellPanel(state, onEvent)
    if (state.emptyCells > 0) WarningText(stringResource(Res.string.combo_warning_empty, state.emptyCells))
    state.problems.forEach { WarningText(stringResource(Res.string.combo_warning_chain, combo.label(it))) }
    OptionalSkillsEditor(state, onEvent, loadDetails)
}
