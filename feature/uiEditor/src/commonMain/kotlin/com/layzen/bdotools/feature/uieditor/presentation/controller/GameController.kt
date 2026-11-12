package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.core.game.isGameRunning
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsError
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsException
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.game.GameSettingsInteractor
import com.layzen.bdotools.feature.uieditor.domain.preset.PresetsInteractor
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsNotice
import org.jetbrains.compose.resources.decodeToImageBitmap

/** Чтение и запись `gamevariable.xml`, выбор аккаунта и подложка редактора. */
internal class GameController(
    private val context: SlotsContext,
    private val interactor: GameSettingsInteractor,
    private val presets: PresetsInteractor,
    private val accounts: GameAccounts,
    private val skills: SkillsController,
) {

    /**
     * Всё, что нужно странице после доступа к папке: файл игры, пресеты и подложка. Аккаунт — выбранный в верхней
     * навигации; пока его не выбрали (или ни в одном файле нет персонажей), — тот, под которым играли последним.
     */
    suspend fun loadAll() {
        accounts.refresh()
        load(accounts.state.value.selected?.id ?: interactor.accounts().first())
    }

    /** Открывает файл другого аккаунта, если страница уже читала папку. */
    fun selectAccount(account: String) {
        val loaded = context.current.loaded ?: return
        if (loaded.account != account) context.launch(busy = true) { load(account) }
    }

    /** Перечитывает файлы открытого аккаунта, теряя несохранённую раскладку. */
    fun reload() {
        val account = context.current.loaded?.account ?: return
        context.launch(busy = true) { load(account) }
    }

    /** Другой набор позиций игры в редактор: общий или «Пресет 1–3». Прежнюю раскладку вернёт «Отменить». */
    fun selectGamePreset(preset: GamePreset) {
        val loaded = context.current.loaded?.takeIf { preset in it.presets } ?: return
        val switched = loaded.copy(edited = preset)
        context.update { it.copy(loaded = switched) }
        context.editLayout(PresetRef.Game) { it.withSkillsOf(switched.game.layout) }
    }

    /** По умолчанию раскладка пишется в тот набор, который открыт в редакторе. */
    fun openApply() {
        val edited = context.current.loaded?.edited ?: return
        context.update { it.copy(dialog = SlotsDialog.ConfirmApply(edited, gameRunning = isGameRunning() == true)) }
    }

    /** Меняет в диалоге применения набор позиций, в который запишется раскладка. */
    fun selectApplyTarget(preset: GamePreset) = updateApplyDialog { it.copy(target = preset) }

    /** Снимает или ставит в диалоге применения отметку персонажа [characterNo]: без отметки его слоты не запишутся. */
    fun toggleApplyCharacter(characterNo: String) = updateApplyDialog {
        it.copy(skipped = if (characterNo in it.skipped) it.skipped - characterNo else it.skipped + characterNo)
    }

    /**
     * Записывает раскладку в выбранный набор, после записи он и открыт в редакторе. Если игра успела переписать
     * файл, ничего не пишется: перечитываем файл, но оставляем раскладку — её можно применить ещё раз, закрыв игру.
     */
    fun confirmApply() = context.launch(busy = true) {
        val loaded = context.current.loaded ?: return@launch
        val dialog = context.current.dialog as? SlotsDialog.ConfirmApply ?: return@launch
        if (isGameRunning() == true) {
            // Игра перезапишет файл при выходе: окно остаётся открытым с предупреждением, пока её не закроют
            context.update { it.copy(dialog = dialog.copy(gameRunning = true)) }
            return@launch
        }
        val target = dialog.target
        context.update { it.copy(dialog = null) }
        try {
            val backup = interactor.apply(loaded, context.current.layout, target)
            skills.write(loaded.account, dialog.skipped)
            refresh(loaded.account, target)
            context.update { it.copy(activePreset = PresetRef.Game, notice = SlotsNotice.Applied(backup), canUndoApply = true) }
        } catch (e: SlotsException) {
            if (e.error == SlotsError.FileChanged) refresh(loaded.account, loaded.edited)
            throw e
        }
    }

    /**
     * Возвращает файлы игры к состоянию до последнего применения и перечитывает их; раскладка в редакторе
     * остаётся, её можно применить снова.
     */
    fun confirmUndoApply() = context.launch(busy = true) {
        val loaded = context.current.loaded ?: return@launch
        context.update { it.copy(dialog = null) }
        if (isGameRunning() == true) {
            context.update { it.copy(notice = SlotsNotice.GameRunning) }
            return@launch
        }
        interactor.undoLastApply(loaded.account)
        refresh(loaded.account, loaded.edited)
        skills.load(loaded.account)
        context.update { it.copy(canUndoApply = false, notice = SlotsNotice.ApplyUndone) }
    }

    /** Даёт выбрать картинку-подложку, сохраняет её для аккаунта и показывает на холсте. */
    fun pickBackground() = context.launch {
        val account = context.current.loaded?.account ?: return@launch
        val bytes = interactor.pickImage() ?: return@launch
        val image = decodeOrNull(bytes) ?: throw SlotsException(SlotsError.NotImage)
        interactor.writeBackground(account, bytes)
        context.update { it.copy(background = image, backgroundShown = true) }
    }

    /** Спрятать подложку или показать снова: картинка остаётся загруженной, выбирать её заново не нужно. */
    fun toggleBackground() = context.update { it.copy(backgroundShown = !it.backgroundShown) }

    /** Задаёт затемнение подложки в процентах. */
    fun setBackgroundDim(percent: Int) = context.update { it.copy(backgroundDim = percent) }

    /**
     * Читает файл аккаунта, его пресеты и подложку; открытым остаётся тот же набор позиций, если он есть в файле.
     * Навыки персонажей грузятся следом в фоне ([SkillsController.load]).
     * Пресеты и подложка из версий до аккаунтов сначала переезжают в аккаунт, который открыли первым.
     */
    private suspend fun load(account: String) {
        val loaded = interactor.load(account, context.current.loaded?.edited ?: GamePreset.General)
        val presets = presets.presets(account)
        val background = interactor.readBackground(account)?.let(::decodeOrNull)
        context.update {
            it.copy(
                loaded = loaded,
                layout = loaded.game.layout,
                history = emptyList(),
                future = emptyList(),
                selection = emptySet(),
                selectedChats = emptySet(),
                activePreset = PresetRef.Game,
                presets = presets,
                background = background,
                canUndoApply = it.canUndoApply && it.loaded?.account == account,
            )
        }
        skills.load(account)
    }

    /** Перечитывает файл, не трогая раскладку в редакторе; в редакторе — набор [edited]. */
    private suspend fun refresh(account: String, edited: GamePreset) {
        val loaded = interactor.refresh(account, edited)
        context.update { it.copy(loaded = loaded) }
    }

    private fun updateApplyDialog(change: (SlotsDialog.ConfirmApply) -> SlotsDialog.ConfirmApply) = context.update {
        val dialog = it.dialog as? SlotsDialog.ConfirmApply
        if (dialog == null) it else it.copy(dialog = change(dialog))
    }

    private fun decodeOrNull(bytes: ByteArray) = try {
        bytes.decodeToImageBitmap()
    } catch (e: Exception) {
        null
    }
}
