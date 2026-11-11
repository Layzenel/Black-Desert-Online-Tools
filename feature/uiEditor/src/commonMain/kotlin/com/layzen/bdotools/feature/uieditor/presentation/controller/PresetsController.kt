package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.BuiltInLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.PresetsInteractor
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsNotice

/** Готовые пресеты и пресеты пользователя. */
internal class PresetsController(
    private val context: SlotsContext,
    private val interactor: PresetsInteractor,
) {

    /** Возвращает в редактор раскладку, прочитанную из игры. */
    fun useGameLayout() {
        val game = context.current.loaded?.game ?: return
        context.editLayout(PresetRef.Game) { game.layout }
    }

    /** Накладывает встроенную раскладку [layout] на текущую. */
    fun useBuiltIn(layout: BuiltInLayout) = context.editLayout(PresetRef.BuiltIn(layout)) { it.overlaidWith(layout.layout) }

    /** Пресет ставится на текущий экран от своих якорей. */
    fun useUserPreset(name: String) {
        val preset = context.current.presets.firstOrNull { it.name == name } ?: return
        val screen = screen() ?: return
        context.editLayout(PresetRef.User(name)) { it.overlaidWith(preset.layout.placeOn(screen)) }
    }

    /** Открывает диалог сохранения пресета с названием активного пользовательского пресета. */
    fun openSaveAs() = context.update { it.copy(dialog = SlotsDialog.SaveAs(activeUserPreset().orEmpty())) }

    /** Запоминает название пресета, введённое в диалоге сохранения. */
    fun changeSaveAsName(name: String) = context.update { it.copy(dialog = SlotsDialog.SaveAs(name)) }

    /** Сохраняет раскладку; пресет с тем же названием перезаписывается. */
    fun confirmSaveAs() {
        val name = (context.current.dialog as? SlotsDialog.SaveAs)?.name?.trim().orEmpty()
        val screen = screen()
        val account = context.current.loaded?.account
        if (name.isEmpty() || screen == null || account == null) return
        context.launch(busy = true) {
            val presets = interactor.save(account, NamedLayout(name, AnchoredLayout.of(context.current.layout, screen)))
            context.update {
                it.copy(presets = presets, activePreset = PresetRef.User(name), dialog = null, notice = SlotsNotice.PresetSaved(name))
            }
        }
    }

    /** Открывает подтверждение удаления пресета [name]. */
    fun requestDelete(name: String) = context.update { it.copy(dialog = SlotsDialog.ConfirmDelete(name)) }

    /** Удаляет пресет из диалога подтверждения и обновляет список пресетов. */
    fun confirmDelete() {
        val name = (context.current.dialog as? SlotsDialog.ConfirmDelete)?.name ?: return
        val account = context.current.loaded?.account ?: return
        context.launch(busy = true) {
            val presets = interactor.delete(account, name)
            context.update { state ->
                val active = state.activePreset.takeUnless { it == PresetRef.User(name) }
                state.copy(presets = presets, activePreset = active, dialog = null, notice = SlotsNotice.PresetDeleted(name))
            }
        }
    }

    private fun screen(): ScreenSpace? = context.current.loaded?.game?.screen

    private fun activeUserPreset(): String? = (context.current.activePreset as? PresetRef.User)?.name
}
