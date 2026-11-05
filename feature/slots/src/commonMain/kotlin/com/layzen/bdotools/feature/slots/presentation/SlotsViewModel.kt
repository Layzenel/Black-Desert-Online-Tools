package com.layzen.bdotools.feature.slots.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.slots.data.DeviceFiles
import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.slots.data.SlotsRepository
import com.layzen.bdotools.feature.slots.data.SlotsStorage
import com.layzen.bdotools.feature.slots.presentation.controller.EditorController
import com.layzen.bdotools.feature.slots.presentation.controller.GameController
import com.layzen.bdotools.feature.slots.presentation.controller.LayoutFilesController
import com.layzen.bdotools.feature.slots.presentation.controller.PresetsController
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Страница раскладки слотов: сама отвечает за доступ к папке, остальное раздаёт контроллерам. */
class SlotsViewModel(
    private val storage: SlotsStorage,
    device: DeviceFiles,
    repository: SlotsRepository,
    private val accounts: GameAccounts,
) : ViewModel() {

    private val context = SlotsContext(viewModelScope)
    private val editor = EditorController(context)
    private val presets = PresetsController(context, repository)
    private val files = LayoutFilesController(context, repository, device)
    private val game = GameController(context, storage, repository, device, accounts)

    val state: StateFlow<SlotsState> = context.state

    init {
        context.launch(busy = true) {
            val access = storage.access()
            context.update { it.copy(access = access) }
            if (access is FolderAccess.Granted) game.loadAll()
        }
        viewModelScope.launch {
            accounts.state.map { it.selected?.id }.filterNotNull().distinctUntilChanged().collect(game::selectAccount)
        }
    }

    fun onEvent(event: SlotsEvent) {
        when (event) {
            SlotsEvent.SelectFolder -> grantAccess { storage.selectFolder() }
            SlotsEvent.RequestPermission -> grantAccess { storage.requestPermission() }
            SlotsEvent.Reload -> game.reload()
            is SlotsEvent.SelectGamePreset -> game.selectGamePreset(event.preset)

            is SlotsEvent.Select -> editor.select(event.id, event.additive)
            SlotsEvent.ClearSelection -> editor.clearSelection()
            is SlotsEvent.SelectArea -> editor.selectArea(event.area, event.additive)
            is SlotsEvent.DragStart -> editor.dragStart(event.id, event.additive)
            is SlotsEvent.DragBy -> editor.dragBy(event.dx, event.dy)
            SlotsEvent.DragEnd -> editor.dragEnd()
            is SlotsEvent.Nudge -> editor.nudge(event.dx, event.dy, event.byGrid)
            is SlotsEvent.SetPosition -> editor.setPosition(event.id, event.position)
            SlotsEvent.Undo -> editor.undo()
            SlotsEvent.Redo -> editor.redo()
            is SlotsEvent.SetGrid -> editor.setGrid(event.step)
            is SlotsEvent.SetSnapStep -> editor.setSnapStep(event.step)
            is SlotsEvent.SetSnapToSlots -> editor.setSnapToSlots(event.enabled)
            is SlotsEvent.SetSnapDistance -> editor.setSnapDistance(event.distance)
            is SlotsEvent.Align -> editor.align(event.edge)
            is SlotsEvent.Distribute -> editor.distribute(event.horizontal)
            SlotsEvent.CenterOnCrosshair -> editor.centerOnCrosshair()
            SlotsEvent.Mirror -> editor.mirror()
            is SlotsEvent.ToggleVisibility -> editor.toggleVisibility(event.id)

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
            is SlotsEvent.PreviewInclude -> files.include(event.ids, event.include)
            is SlotsEvent.PreviewArea -> files.includeArea(event.area)
            is SlotsEvent.PreviewScreen -> files.setPreviewScreen(event.screen)
            SlotsEvent.PreviewUndo -> files.undoPreview()
            SlotsEvent.PreviewRedo -> files.redoPreview()
            SlotsEvent.ConfirmPreview -> files.confirmPreview()

            SlotsEvent.OpenApply -> game.openApply()
            is SlotsEvent.SelectApplyTarget -> game.selectApplyTarget(event.preset)
            SlotsEvent.ConfirmApply -> game.confirmApply()
            SlotsEvent.PickBackground -> game.pickBackground()
            SlotsEvent.ToggleBackground -> game.toggleBackground()

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
