package com.layzen.bdotools.feature.slots.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.slots.data.DeviceFiles
import com.layzen.bdotools.feature.slots.data.FolderAccess
import com.layzen.bdotools.feature.slots.data.SlotsRepository
import com.layzen.bdotools.feature.slots.data.SlotsStorage
import com.layzen.bdotools.feature.slots.presentation.controller.EditorController
import com.layzen.bdotools.feature.slots.presentation.controller.GameController
import com.layzen.bdotools.feature.slots.presentation.controller.PresetsController
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import kotlinx.coroutines.flow.StateFlow

/** Страница раскладки слотов: сама отвечает за доступ к папке, остальное раздаёт контроллерам. */
class SlotsViewModel(
    private val storage: SlotsStorage,
    device: DeviceFiles,
    repository: SlotsRepository,
) : ViewModel() {

    private val context = SlotsContext(viewModelScope)
    private val editor = EditorController(context)
    private val presets = PresetsController(context, repository, device)
    private val game = GameController(context, storage, repository, device)

    val state: StateFlow<SlotsState> = context.state

    init {
        context.launch(busy = true) {
            val access = storage.access()
            context.update { it.copy(access = access) }
            if (access is FolderAccess.Granted) game.loadAll()
        }
    }

    fun onEvent(event: SlotsEvent) {
        when (event) {
            SlotsEvent.SelectFolder -> grantAccess { storage.selectFolder() }
            SlotsEvent.RequestPermission -> grantAccess { storage.requestPermission() }
            is SlotsEvent.SelectAccount -> game.selectAccount(event.account)
            SlotsEvent.Reload -> game.reload()

            is SlotsEvent.Select -> editor.select(event.id, event.additive)
            SlotsEvent.ClearSelection -> editor.clearSelection()
            is SlotsEvent.DragStart -> editor.dragStart(event.id, event.additive)
            is SlotsEvent.DragBy -> editor.dragBy(event.dx, event.dy)
            SlotsEvent.DragEnd -> editor.dragEnd()
            is SlotsEvent.Nudge -> editor.nudge(event.dx, event.dy, event.byGrid)
            is SlotsEvent.SetPosition -> editor.setPosition(event.id, event.position)
            SlotsEvent.Mirror -> editor.mirror()
            SlotsEvent.Undo -> editor.undo()
            is SlotsEvent.SetGrid -> editor.setGrid(event.step)
            is SlotsEvent.SetSnapStep -> editor.setSnapStep(event.step)
            is SlotsEvent.ToggleVisibility -> editor.toggleVisibility(event.id)

            SlotsEvent.UseGameLayout -> presets.useGameLayout()
            is SlotsEvent.UseBuiltIn -> presets.useBuiltIn(event.layout)
            is SlotsEvent.UseUserPreset -> presets.useUserPreset(event.name)
            SlotsEvent.OpenSaveAs -> presets.openSaveAs()
            is SlotsEvent.SaveAsNameChanged -> presets.changeSaveAsName(event.name)
            SlotsEvent.ConfirmSaveAs -> presets.confirmSaveAs()
            is SlotsEvent.RequestDelete -> presets.requestDelete(event.name)
            SlotsEvent.ConfirmDelete -> presets.confirmDelete()
            SlotsEvent.OpenImport -> presets.openImport()
            is SlotsEvent.ImportTextChanged -> presets.changeImportText(event.text)
            SlotsEvent.ImportFromFile -> presets.importFromFile()
            SlotsEvent.ConfirmImport -> presets.confirmImport()
            SlotsEvent.Export -> presets.export()

            SlotsEvent.OpenApply -> game.openApply()
            SlotsEvent.ConfirmApply -> game.confirmApply()
            SlotsEvent.PickBackground -> game.pickBackground()
            SlotsEvent.ClearBackground -> game.clearBackground()

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
