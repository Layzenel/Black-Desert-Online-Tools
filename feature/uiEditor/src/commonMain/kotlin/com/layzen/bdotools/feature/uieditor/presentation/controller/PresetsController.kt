package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.data.SlotsRepository
import com.layzen.bdotools.feature.uieditor.domain.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.BuiltInLayout
import com.layzen.bdotools.feature.uieditor.domain.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsNotice

/** Готовые пресеты и пресеты пользователя. */
internal class PresetsController(
    private val context: SlotsContext,
    private val repository: SlotsRepository,
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

    private fun screen(): ScreenSpace? = context.current.loaded?.game?.screen

    private fun activeUserPreset(): String? = (context.current.activePreset as? PresetRef.User)?.name
}
