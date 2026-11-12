package com.layzen.bdotools.feature.uieditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutFilesInteractor
import com.layzen.bdotools.feature.uieditor.domain.game.GameSettingsInteractor
import com.layzen.bdotools.feature.uieditor.domain.preset.PresetsInteractor
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillsInteractor
import com.layzen.bdotools.feature.uieditor.presentation.controller.ChatsController
import com.layzen.bdotools.feature.uieditor.presentation.controller.EditorController
import com.layzen.bdotools.feature.uieditor.presentation.controller.GameController
import com.layzen.bdotools.feature.uieditor.presentation.controller.GroupSpreadController
import com.layzen.bdotools.feature.uieditor.presentation.controller.LayoutFilesController
import com.layzen.bdotools.feature.uieditor.presentation.controller.PresetsController
import com.layzen.bdotools.feature.uieditor.presentation.controller.SkillEditController
import com.layzen.bdotools.feature.uieditor.presentation.controller.SkillsController
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsNotice
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Страница раскладки слотов: сама отвечает за доступ к папке, остальное раздаёт контроллерам. */
class SlotsViewModel(
    private val gameSettings: GameSettingsInteractor,
    userPresets: PresetsInteractor,
    layoutFiles: LayoutFilesInteractor,
    skillsInteractor: SkillsInteractor,
    private val accounts: GameAccounts,
) : ViewModel() {

    private val context = SlotsContext(viewModelScope)
    private val editor = EditorController(context)
    private val chats = ChatsController(context)
    private val spread = GroupSpreadController(context)
    private val presets = PresetsController(context, userPresets)
    private val skills = SkillsController(context, skillsInteractor, accounts)
    private val skillEdit = SkillEditController(context, skills)
    private val files = LayoutFilesController(context, layoutFiles, skills)
    private val game = GameController(context, gameSettings, userPresets, accounts, skills)

    /** Состояние экрана; экран его только читает. */
    val state: StateFlow<SlotsState> = context.state

    init {
        context.launch(busy = true) {
            val access = gameSettings.access()
            context.update { it.copy(access = access) }
            if (access is FolderAccess.Granted) game.loadAll()
        }
        viewModelScope.launch {
            accounts.state.map { it.selected?.id }.filterNotNull().distinctUntilChanged().collect(game::selectAccount)
        }
    }

    /** Единая точка входа для действий экрана: раздаёт [event] нужному контроллеру. */
    fun onEvent(event: SlotsEvent) {
        when (event) {
            SlotsEvent.SelectFolder -> grantAccess { gameSettings.selectFolder() }
            SlotsEvent.RequestPermission -> grantAccess { gameSettings.requestPermission() }
            SlotsEvent.Reload -> game.reload()
            SlotsEvent.Shown -> skills.refreshLabels()
            is SlotsEvent.SelectGamePreset -> game.selectGamePreset(event.preset)

            is SlotsEvent.Select -> editor.select(event.id, event.additive)
            SlotsEvent.ClearSelection -> editor.clearSelection()
            is SlotsEvent.SelectArea -> editor.selectArea(event.area, event.additive)
            is SlotsEvent.DragStart -> editor.dragStart(event.id, event.additive)
            is SlotsEvent.SelectChat -> editor.selectChat(event.window, event.additive)
            is SlotsEvent.DragChatStart -> editor.dragChatStart(event.window, event.additive)
            is SlotsEvent.DragBy -> editor.dragBy(event.dx, event.dy)
            SlotsEvent.DragEnd -> editor.dragEnd()
            is SlotsEvent.Nudge -> editor.nudge(event.dx, event.dy, event.byGrid)
            is SlotsEvent.SetPosition -> editor.setPosition(event.id, event.position)
            SlotsEvent.Undo -> editor.undo()
            SlotsEvent.Redo -> editor.redo()
            is SlotsEvent.SetGrid -> editor.setGrid(event.step)
            is SlotsEvent.SetSnapToGrid -> editor.setSnapToGrid(event.enabled)
            is SlotsEvent.SetSnapStep -> editor.setSnapStep(event.step)
            is SlotsEvent.SetSnapToSlots -> editor.setSnapToSlots(event.enabled)
            is SlotsEvent.SetSnapDistance -> editor.setSnapDistance(event.distance)
            is SlotsEvent.Align -> editor.align(event.edge, event.anchor)
            is SlotsEvent.Distribute -> editor.distribute(event.horizontal)
            is SlotsEvent.ResizeChat -> chats.resize(event.window, event.dx, event.dy)
            is SlotsEvent.SetChatSize -> chats.setSize(event.window, event.width, event.height)
            is SlotsEvent.SetChatPosition -> chats.setPosition(event.window, event.center)
            is SlotsEvent.SpreadGroups -> spread.spread(event.horizontal, event.value)
            SlotsEvent.SpreadGroupsEnd -> spread.end()
            SlotsEvent.ChatEditEnd -> chats.endEdit()
            is SlotsEvent.ToggleChatVisibility -> chats.toggleVisibility(event.window)
            is SlotsEvent.DetachChat -> chats.detach(event.window)
            is SlotsEvent.CombineChat -> chats.combine(event.window)
            is SlotsEvent.SetDistributeGap -> editor.setDistributeGap(event.gap, event.horizontal)
            is SlotsEvent.ToggleVisibility -> editor.toggleVisibility(event.id)
            is SlotsEvent.SetSelectionVisibility -> editor.setSelectionVisibility(event.visible)
            is SlotsEvent.SetLabel -> editor.setLabel(event.id, event.chars)
            is SlotsEvent.SetCustomQuickSlots -> editor.setCustomQuickSlots(event.enabled)
            is SlotsEvent.SetSplitHp -> editor.setSplitHp(event.enabled)
            is SlotsEvent.SelectCharacter -> skills.select(event.characterNo)
            is SlotsEvent.OpenMemo -> skillEdit.openMemo(event.id)
            is SlotsEvent.MemoChanged -> skillEdit.changeMemo(event.text)
            SlotsEvent.ConfirmMemo -> skillEdit.confirmMemo()
            is SlotsEvent.DeleteMemo -> skillEdit.deleteMemo(event.id)
            is SlotsEvent.OpenSkillPicker -> skillEdit.openSkillPicker(event.id)
            is SlotsEvent.SkillQueryChanged -> skillEdit.changeQuery(event.query)
            is SlotsEvent.SkillStanceChanged -> skillEdit.changeStance(event.stance)
            is SlotsEvent.SkillHideAbsoluteChanged -> skillEdit.changeHideAbsolute(event.hide)
            is SlotsEvent.SkillHidePveControlsChanged -> skillEdit.changeHidePveControls(event.hide)
            is SlotsEvent.PickSkill -> skillEdit.pickSkill(event.skillNo)
            is SlotsEvent.RemoveSkill -> skillEdit.removeSkill(event.id)
            is SlotsEvent.RequestSkillDetails -> skills.loadDetails(event.skillNo)
            is SlotsEvent.SkillDragStart -> skillEdit.startSkillDrag(event.id)
            is SlotsEvent.SkillDragBy -> skillEdit.skillDragBy(event.dx, event.dy)
            SlotsEvent.SkillDragEnd -> skillEdit.endSkillDrag()
            is SlotsEvent.ToggleImportCharacter -> files.toggleImportCharacter(event.characterNo)
            is SlotsEvent.ConfirmImportCharacters -> files.confirmImportCharacters()
            is SlotsEvent.ImportWithoutSkills -> files.importWithoutSkills()

            SlotsEvent.UseGameLayout -> presets.useGameLayout()
            is SlotsEvent.UseBuiltIn -> presets.useBuiltIn(event.layout)
            is SlotsEvent.UseUserPreset -> presets.useUserPreset(event.name)
            SlotsEvent.OpenSaveAs -> presets.openSaveAs()
            is SlotsEvent.SaveAsNameChanged -> presets.changeSaveAsName(event.name)
            SlotsEvent.ConfirmSaveAs -> presets.confirmSaveAs()
            is SlotsEvent.RequestDelete -> presets.requestDelete(event.name)
            SlotsEvent.ConfirmDelete -> presets.confirmDelete()
            SlotsEvent.OpenImport -> files.openImport()
            is SlotsEvent.ImportTextChanged -> files.changeImportText(event.text)
            SlotsEvent.ImportFromFile -> files.importFromFile()
            SlotsEvent.ConfirmImport -> files.confirmImport()
            SlotsEvent.Export -> files.export()
            is SlotsEvent.PreviewInclude -> files.include(event.ids, event.chats, event.include)
            is SlotsEvent.PreviewArea -> files.includeArea(event.area)
            is SlotsEvent.PreviewScreen -> files.setPreviewScreen(event.screen)
            is SlotsEvent.PreviewCustomQuickSlots -> files.setPreviewQuickSlots(event.enabled)
            is SlotsEvent.PreviewSplitHp -> files.setPreviewSplitHp(event.enabled)
            is SlotsEvent.PreviewSkills -> files.setPreviewSkills(event.enabled)
            is SlotsEvent.PreviewMemos -> files.setPreviewMemos(event.enabled)
            SlotsEvent.PreviewUndo -> files.undoPreview()
            SlotsEvent.PreviewRedo -> files.redoPreview()
            SlotsEvent.ConfirmPreview -> files.confirmPreview()

            SlotsEvent.OpenApply -> game.openApply()
            is SlotsEvent.SelectApplyTarget -> game.selectApplyTarget(event.preset)
            is SlotsEvent.ToggleApplyCharacter -> game.toggleApplyCharacter(event.characterNo)
            SlotsEvent.ConfirmApply -> game.confirmApply()
            SlotsEvent.OpenUndoApply -> context.update { it.copy(dialog = SlotsDialog.ConfirmUndoApply) }
            SlotsEvent.ConfirmUndoApply -> game.confirmUndoApply()
            SlotsEvent.CopyPreview -> files.copyPreview()
            SlotsEvent.LayoutCopied -> context.update { it.copy(copiedLayout = null, notice = SlotsNotice.LayoutCopied) }
            SlotsEvent.PickBackground -> game.pickBackground()
            SlotsEvent.ToggleBackground -> game.toggleBackground()
            is SlotsEvent.SetBackgroundDim -> game.setBackgroundDim(event.percent)
            is SlotsEvent.SetSkillFrameTransparency -> context.update { it.copy(skillFrameTransparency = event.percent) }

            SlotsEvent.CloseDialog -> context.update { it.copy(dialog = null) }
            SlotsEvent.DismissNotice -> context.update { it.copy(notice = null) }
        }
    }

    private fun grantAccess(request: suspend () -> FolderAccess.Granted) = context.launch(busy = true) {
        val access = request()
        context.update { it.copy(access = access, notice = null) }
        game.loadAll()
    }
}
