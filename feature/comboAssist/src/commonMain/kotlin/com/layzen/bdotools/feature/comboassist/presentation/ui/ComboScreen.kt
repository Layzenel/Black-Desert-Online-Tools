package com.layzen.bdotools.feature.comboassist.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.PaletteDrag
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.PaletteDragGhost
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import com.layzen.bdotools.feature.comboassist.presentation.ui.editor.OptionalSkillsEditor
import com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar.ComboSidebarContent
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlaysettings.OverlaySettingsPanel
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_delete
import com.layzen.bdotools.feature.comboassist.resources.combo_delete_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_export
import com.layzen.bdotools.feature.comboassist.resources.combo_export_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_import_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_open_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_remove_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_save_tooltip
import com.layzen.bdotools.feature.comboassist.resources.menu_delete
import com.layzen.bdotools.feature.comboassist.resources.menu_redo
import com.layzen.bdotools.feature.comboassist.resources.menu_undo
import com.layzen.bdotools.feature.comboassist.resources.combo_redo_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_undo_tooltip
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.components.ToolbarDivider
import com.layzen.bdotools.feature.comboassist.resources.menu_discard
import com.layzen.bdotools.feature.comboassist.resources.menu_rage_add
import com.layzen.bdotools.feature.comboassist.resources.menu_copy
import com.layzen.bdotools.feature.comboassist.resources.combo_discard_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_add_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_rage_copy_tooltip
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
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.TabBar
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
    val paletteDrag = remember { PaletteDrag() }
    // начало страницы в окне: иконка перетаскиваемого умения рисуется поверх неё в координатах окна
    var origin by remember { mutableStateOf(Offset.Zero) }
    Box(Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInRoot() }) {
    ToolPage(
        modifier = Modifier.fillMaxSize().focusRequester(focus).onKeyEvent { event ->
            // Ctrl+Z — отменить, Ctrl+Y и Ctrl+Shift+Z — повторить, как в редакторе интерфейса
            val action = when {
                !event.isCtrlPressed && !event.isMetaPressed -> null
                event.key == Key.Z && event.isShiftPressed -> ComboEvent.Redo
                event.key == Key.Z -> ComboEvent.Undo
                event.key == Key.Y -> ComboEvent.Redo
                else -> null
            }
            if (action != null && event.type == KeyEventType.KeyDown) onEvent(action)
            action != null
        }.focusable(),
        sidebarWidth = ComboDimens.sidebarWidth,
        sidebar = { ComboSidebarContent(state, onEvent, loadDetails, paletteDrag) },
        sidebarFooter = {
            val unnamed = needsName(state.combo?.name.orEmpty())
            if (canWatchGame) ActionButton(ActionButtonState.Neutral(stringResource(Res.string.combo_notice_open), icon = painterResource(Res.drawable.menu_info), onClick = { onEvent(ComboEvent.ShowTrackerNotice) }, tooltip = stringResource(Res.string.combo_notice_open_tooltip)), Modifier.fillMaxWidth())
            if (state.importFailed) WarningText(stringResource(Res.string.combo_import_failed))
            // работа с файлом — одним рядом: подписи короткие, подробности в подсказках
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                ActionButton(ActionButtonState.Neutral(stringResource(Res.string.combo_import), icon = painterResource(Res.drawable.menu_import), onClick = { onEvent(ComboEvent.ImportCombos) }, tooltip = stringResource(Res.string.combo_import_tooltip)), Modifier.weight(1f))
                ActionButton(ActionButtonState.Neutral(stringResource(Res.string.combo_export), icon = painterResource(Res.drawable.menu_export), enabled = state.combo != null, onClick = { onEvent(ComboEvent.ExportCombo) }, tooltip = stringResource(Res.string.combo_export_tooltip)), Modifier.weight(1f))
            }
            if (state.combo?.rage != null) {
                ActionButton(ActionButtonState.Negative(stringResource(Res.string.combo_rage_remove), icon = painterResource(Res.drawable.menu_delete), onClick = { onEvent(ComboEvent.RemoveRage) }, tooltip = stringResource(Res.string.combo_rage_remove_tooltip)), Modifier.fillMaxWidth())
            }
            ActionButton(
                ActionButtonState.Negative(
                    text = stringResource(Res.string.combo_delete),
                    icon = painterResource(Res.drawable.menu_delete),
                    enabled = state.combo != null,
                    onClick = { onEvent(ComboEvent.AskDelete) },
                    tooltip = stringResource(Res.string.combo_delete_tooltip)
                ),
                Modifier.fillMaxWidth(),
            )
            ActionButton(
                ActionButtonState.Positive(
                    text = stringResource(if (state.dirty) Res.string.combo_save_dirty else Res.string.combo_save),
                    icon = painterResource(Res.drawable.menu_save),
                    enabled = state.combo != null,
                    onClick = { onEvent(if (unnamed) ComboEvent.AskName else ComboEvent.Save) },
                    tooltip = stringResource(Res.string.combo_save_tooltip),
                ),
                Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            // настройка оверлея есть только в настольной версии: на сайте остаётся один редактор и переключать нечего
            if (canWatchGame) ActionBar {
                TabBar(ComboMode.entries, state.mode, { onEvent(ComboEvent.SetMode(it)) }, { stringResource(if (it == ComboMode.Editor) Res.string.combo_mode_editor else Res.string.combo_mode_overlay) })
            }
            // сброс, отмена и повтор правок — под табами, как в редакторе интерфейса, и Ctrl+Z / Ctrl+Y
            ActionBar { EditorTools(state, onEvent) }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppTheme.dimens.spaceL),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL),
            ) {
                if (state.mode == ComboMode.Overlay && canWatchGame) OverlaySettingsPanel(state, onEvent, loadDetails)
                else EditorContent(state, onEvent, loadDetails, paletteDrag)
            }
        }
    }
    PaletteDragGhost(paletteDrag, origin) { state.skills[it]?.icon }
    }
    state.combo?.takeIf { state.nameAsked }?.let { combo ->
        ComboNameDialog(combo.name, onCancel = { onEvent(ComboEvent.CancelName) }, onSave = { onEvent(ComboEvent.SaveAs(it)) })
    }
    if (state.trackerNotice) TrackerNoticeDialog { onEvent(ComboEvent.CloseTrackerNotice) }
    val deleted = state.combo
    if (state.deleteAsked && deleted != null) {
        val name = deleted.name.ifBlank { stringResource(Res.string.combo_untitled) }
        DeleteComboDialog(name, onCancel = { onEvent(ComboEvent.CancelDelete) }, onConfirm = { onEvent(ComboEvent.Delete) })
    }
}

/**
 * Инструменты над редактором: сброс к сохранённому, отмена, повтор; у комбо без варианта под гневом — «добавить
 * комбо под гневом», на вкладке гнева — «скопировать обычное комбо». Без подписей, что делают — в подсказках.
 */
@Composable
private fun EditorTools(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    ToolbarButton(painterResource(Res.drawable.menu_discard), stringResource(Res.string.combo_discard_tooltip), { onEvent(ComboEvent.Discard) }, enabled = state.dirty)
    ToolbarButton(painterResource(Res.drawable.menu_undo), stringResource(Res.string.combo_undo_tooltip), { onEvent(ComboEvent.Undo) }, enabled = state.canUndo)
    ToolbarButton(painterResource(Res.drawable.menu_redo), stringResource(Res.string.combo_redo_tooltip), { onEvent(ComboEvent.Redo) }, enabled = state.canRedo)
    val combo = state.combo ?: return
    if (state.mode != ComboMode.Editor) return
    ToolbarDivider()
    if (combo.rage == null) {
        ToolbarButton(painterResource(Res.drawable.menu_rage_add), stringResource(Res.string.combo_rage_add_tooltip), { onEvent(ComboEvent.AddRage) })
    } else if (state.rageEdited) {
        // комбо под гневом обычно — то же комбо с парой замен: копия обычного, а дальше правятся отличия
        ToolbarButton(painterResource(Res.drawable.menu_copy), stringResource(Res.string.combo_rage_copy_tooltip), { onEvent(ComboEvent.CopyToRage) })
    }
}

@Composable
private fun EditorContent(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?, paletteDrag: PaletteDrag) {
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
    ) { ComboCanvas(state, onEvent, loadDetails, paletteDrag) }
    CellPanel(state, onEvent, loadDetails)
    if (state.emptyCells > 0) WarningText(stringResource(Res.string.combo_warning_empty, state.emptyCells))
    state.problems.forEach { WarningText(stringResource(Res.string.combo_warning_chain, combo.label(it))) }
    OptionalSkillsEditor(state, onEvent, loadDetails)
}
