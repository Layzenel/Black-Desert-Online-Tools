package com.layzen.bdotools.feature.slots.presentation.controller

import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.feature.slots.data.DeviceFiles
import com.layzen.bdotools.feature.slots.data.SlotsError
import com.layzen.bdotools.feature.slots.data.SlotsException
import com.layzen.bdotools.feature.slots.data.SlotsRepository
import com.layzen.bdotools.feature.slots.data.SlotsStorage
import com.layzen.bdotools.feature.slots.domain.GamePreset
import com.layzen.bdotools.feature.slots.presentation.SlotsContext
import com.layzen.bdotools.feature.slots.presentation.state.PresetRef
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.state.SlotsNotice
import org.jetbrains.compose.resources.decodeToImageBitmap

/** Чтение и запись `gamevariable.xml`, выбор аккаунта и подложка редактора. */
internal class GameController(
    private val context: SlotsContext,
    private val storage: SlotsStorage,
    private val repository: SlotsRepository,
    private val device: DeviceFiles,
    private val accounts: GameAccounts,
) {

    /**
     * Всё, что нужно странице после доступа к папке: файл игры, пресеты и подложка. Аккаунт — выбранный в верхней
     * навигации; пока его не выбрали (или ни в одном файле нет персонажей), — тот, под которым играли последним.
     */
    suspend fun loadAll() {
        accounts.refresh()
        load(accounts.state.value.selected?.id ?: repository.accounts().first())
        val presets = repository.presets()
        val background = storage.readBackground()?.let(::decodeOrNull)
        context.update { it.copy(presets = presets, background = background) }
    }

    /** Открывает файл другого аккаунта, если страница уже читала папку. */
    fun selectAccount(account: String) {
        val loaded = context.current.loaded ?: return
        if (loaded.account != account) context.launch(busy = true) { load(account) }
    }

    fun reload() {
        val account = context.current.loaded?.account ?: return
        context.launch(busy = true) { load(account) }
    }

    /** Другой набор позиций игры в редактор: общий или «Пресет 1–3». Прежнюю раскладку вернёт «Отменить». */
    fun selectGamePreset(preset: GamePreset) {
        val loaded = context.current.loaded?.takeIf { preset in it.presets } ?: return
        val switched = loaded.copy(edited = preset)
        context.update { it.copy(loaded = switched) }
        context.editLayout(PresetRef.Game) { switched.game.layout }
    }

    /** По умолчанию раскладка пишется в тот набор, который открыт в редакторе. */
    fun openApply() {
        val edited = context.current.loaded?.edited ?: return
        context.update { it.copy(dialog = SlotsDialog.ConfirmApply(edited)) }
    }

    fun selectApplyTarget(preset: GamePreset) = context.update {
        if (it.dialog is SlotsDialog.ConfirmApply) it.copy(dialog = SlotsDialog.ConfirmApply(preset)) else it
    }

    /**
     * Записывает раскладку в выбранный набор, после записи он и открыт в редакторе. Если игра успела переписать
     * файл, ничего не пишется: перечитываем файл, но оставляем раскладку — её можно применить ещё раз, закрыв игру.
     */
    fun confirmApply() = context.launch(busy = true) {
        val loaded = context.current.loaded ?: return@launch
        val target = (context.current.dialog as? SlotsDialog.ConfirmApply)?.target ?: return@launch
        context.update { it.copy(dialog = null) }
        try {
            val backup = repository.apply(loaded, context.current.layout, target)
            refresh(loaded.account, target)
            context.update { it.copy(activePreset = PresetRef.Game, notice = SlotsNotice.Applied(backup)) }
        } catch (e: SlotsException) {
            if (e.error == SlotsError.FileChanged) refresh(loaded.account, loaded.edited)
            throw e
        }
    }

    fun pickBackground() = context.launch {
        val bytes = device.pickImage() ?: return@launch
        val image = decodeOrNull(bytes) ?: throw SlotsException(SlotsError.NotImage)
        storage.writeBackground(bytes)
        context.update { it.copy(background = image, backgroundShown = true) }
    }

    /** Спрятать подложку или показать снова: картинка остаётся загруженной, выбирать её заново не нужно. */
    fun toggleBackground() = context.update { it.copy(backgroundShown = !it.backgroundShown) }

    fun setBackgroundDim(percent: Int) = context.update { it.copy(backgroundDim = percent) }

    /** Читает файл аккаунта; открытым остаётся тот же набор позиций, если он есть в файле. */
    private suspend fun load(account: String) {
        val loaded = repository.load(account, context.current.loaded?.edited ?: GamePreset.General)
        context.update {
            it.copy(loaded = loaded, layout = loaded.game.layout, history = emptyList(), future = emptyList(), selection = emptySet(), activePreset = PresetRef.Game)
        }
    }

    /** Перечитывает файл, не трогая раскладку в редакторе; в редакторе — набор [edited]. */
    private suspend fun refresh(account: String, edited: GamePreset) {
        val loaded = repository.load(account, edited)
        context.update { it.copy(loaded = loaded) }
    }

    private fun decodeOrNull(bytes: ByteArray) = try {
        bytes.decodeToImageBitmap()
    } catch (e: Exception) {
        null
    }
}
