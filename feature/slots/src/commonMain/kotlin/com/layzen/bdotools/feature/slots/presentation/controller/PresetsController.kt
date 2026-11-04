package com.layzen.bdotools.feature.slots.presentation.controller

import com.layzen.bdotools.feature.slots.data.DeviceFiles
import com.layzen.bdotools.feature.slots.data.SlotsRepository
import com.layzen.bdotools.feature.slots.domain.AnchoredLayout
import com.layzen.bdotools.feature.slots.domain.BuiltInLayout
import com.layzen.bdotools.feature.slots.domain.LayoutCodec
import com.layzen.bdotools.feature.slots.domain.LayoutFormatException
import com.layzen.bdotools.feature.slots.domain.NamedLayout
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.SlotsContext
import com.layzen.bdotools.feature.slots.presentation.state.PresetRef
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.state.SlotsNotice

/** Имя файла экспорта, если раскладка не из сохранённого пресета. */
private const val DEFAULT_EXPORT_NAME = "slot-layout"

/** Готовые пресеты, пресеты пользователя, импорт и экспорт. */
internal class PresetsController(
    private val context: SlotsContext,
    private val repository: SlotsRepository,
    private val device: DeviceFiles,
) {

    fun useGameLayout() {
        val game = context.current.loaded?.game ?: return
        context.editLayout(PresetRef.Game) { game.layout }
    }

    fun useBuiltIn(layout: BuiltInLayout) = context.editLayout(PresetRef.BuiltIn(layout)) { it.overlaidWith(layout.layout) }

    /** Пресет ставится на текущий экран от своих якорей. */
    fun useUserPreset(name: String) {
        val preset = context.current.presets.firstOrNull { it.name == name } ?: return
        val screen = screen() ?: return
        context.editLayout(PresetRef.User(name)) { it.overlaidWith(preset.layout.placeOn(screen)) }
    }

    fun openSaveAs() = context.update { it.copy(dialog = SlotsDialog.SaveAs(activeUserPreset().orEmpty())) }

    fun changeSaveAsName(name: String) = context.update { it.copy(dialog = SlotsDialog.SaveAs(name)) }

    /** Сохраняет раскладку; пресет с тем же названием перезаписывается. */
    fun confirmSaveAs() {
        val name = (context.current.dialog as? SlotsDialog.SaveAs)?.name?.trim().orEmpty()
        val screen = screen()
        if (name.isEmpty() || screen == null) return
        context.launch(busy = true) {
            repository.savePreset(NamedLayout(name, AnchoredLayout.of(context.current.layout, screen)))
            val presets = repository.presets()
            context.update {
                it.copy(presets = presets, activePreset = PresetRef.User(name), dialog = null, notice = SlotsNotice.PresetSaved(name))
            }
        }
    }

    fun requestDelete(name: String) = context.update { it.copy(dialog = SlotsDialog.ConfirmDelete(name)) }

    fun confirmDelete() {
        val name = (context.current.dialog as? SlotsDialog.ConfirmDelete)?.name ?: return
        context.launch(busy = true) {
            repository.deletePreset(name)
            val presets = repository.presets()
            context.update { state ->
                val active = state.activePreset.takeUnless { it == PresetRef.User(name) }
                state.copy(presets = presets, activePreset = active, dialog = null, notice = SlotsNotice.PresetDeleted(name))
            }
        }
    }

    fun openImport() = context.update { it.copy(dialog = SlotsDialog.Import("")) }

    fun changeImportText(text: String) = context.update { it.copy(dialog = SlotsDialog.Import(text)) }

    fun importFromFile() = context.launch {
        val text = device.pickLayoutFile() ?: return@launch
        import(text)
    }

    fun confirmImport() {
        val dialog = context.current.dialog as? SlotsDialog.Import ?: return
        import(dialog.text)
    }

    fun export() = context.launch {
        val screen = screen() ?: return@launch
        val name = activeUserPreset() ?: DEFAULT_EXPORT_NAME
        val layout = NamedLayout(name, AnchoredLayout.of(context.current.layout, screen))
        device.download(repository.fileName(name), LayoutCodec.encode(layout))
    }

    /**
     * Импортированная раскладка ставится на этот экран от своих якорей и ложится поверх текущей: группы,
     * которых нет в файле, не меняются. Если что-то не влезло, сообщение подскажет масштаб интерфейса.
     */
    private fun import(text: String) {
        val screen = screen() ?: return
        val imported = try {
            LayoutCodec.decode(text)
        } catch (e: LayoutFormatException) {
            context.update { it.copy(dialog = SlotsDialog.Import(text, e.error)) }
            return
        }
        context.editLayout { it.overlaidWith(imported.layout.placeOn(screen)) }
        val offScreen = imported.layout.offScreenCount(screen)
        val notice = SlotsNotice.Imported(imported.name, offScreen, imported.layout.suggestedUiScale(screen))
        context.update { it.copy(dialog = null, notice = notice) }
    }

    private fun screen(): ScreenSpace? = context.current.loaded?.game?.screen

    private fun activeUserPreset(): String? = (context.current.activePreset as? PresetRef.User)?.name
}
