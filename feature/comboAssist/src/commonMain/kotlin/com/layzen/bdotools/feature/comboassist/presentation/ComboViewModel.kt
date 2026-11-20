package com.layzen.bdotools.feature.comboassist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboInteractor
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboRage
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyCommand
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyStep
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboStance
import kotlin.concurrent.Volatile
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerSettings
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.presentation.controller.EditorController
import com.layzen.bdotools.feature.comboassist.presentation.controller.TrainingController
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboMode
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Экран комбо: список комбо, редактор, тренировка и слежение за игрой. Правку делает [EditorController],
 * трекер со временем — [TrainingController].
 *
 * @property interactor сценарии комбо
 */
class ComboViewModel(private val interactor: ComboInteractor) : ViewModel() {
    private val editor = EditorController()
    private val training = TrainingController()
    private val mutableState = MutableStateFlow(ComboState())
    private var skills = ComboSkills.Empty
    private var ticker: Job? = null
    private var watcher: Job? = null

    /** Варианты клавиш умений комбо, за которым следят. */
    private var keyCommands: Map<Int, KeyCommand> = emptyMap()

    /** Шаги, которые трекер ждёт сейчас, для потока слежения: он читает их из своего потока. */
    @Volatile private var keySteps: List<KeyStep> = emptyList()
    private var skillsLoader: Job? = null

    /** Комбо до каждой правки, последняя — в конце; правки другого комбо при отмене пропускаются ([undo]). */
    private val history = ArrayDeque<Combo>()

    /** Комбо до каждой отмены, последняя — в конце, для Ctrl+Y ([redo]); новая правка их забывает. */
    private val future = ArrayDeque<Combo>()

    /** Состояние экрана. */
    val state: StateFlow<ComboState> = mutableState.asStateFlow()

    /** Какие слоты перезарядок закрыты окном игры (номера по порядку [ComboState.slots]); проверяется, только пока на это подписаны. */
    val hiddenSlots: StateFlow<Set<Int>> = interactor.hiddenSlots().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    /** Может ли платформа следить за игрой и показывать оверлей поверх неё. */
    val canWatchGame: Boolean get() = interactor.canWatchGame

    init {
        viewModelScope.launch {
            val combos = interactor.combos()
            val first = combos.firstOrNull()
            val settings = interactor.settings()
            mutableState.update {
                val characterClass = first?.let(::classOf) ?: it.characterClass
                it.copy(
                    combos = combos, combo = first, settings = settings, characterClass = characterClass, stance = ComboStance.keep(characterClass, it.stance),
                    trackerNotice = interactor.canWatchGame && !settings.trackerNoticeSeen,
                )
            }
            reloadSkills()
            if (interactor.canWatchGame) followPlayedCharacter()
        }
    }

    /** Подробности умения для тултипа и клавиш; справочник кэширует их сам. */
    suspend fun details(skillNo: Int): SkillDetails? = interactor.details(skillNo)

    /** Умения схем при наведении в меню оверлея по набору умений комбо: иконки качаются один раз, правка комбо — новый набор. */
    private val previews = mutableMapOf<Set<Int>, Map<Int, SkillItem>>()

    /** Умения комбо [combo] с иконками — для его схемы при наведении в меню оверлея (класс может быть не открытым). */
    suspend fun previewSkills(combo: Combo): Map<Int, SkillItem> {
        val needed = combo.parts().flatMap { part -> part.cells().mapNotNull { it.second.skillNo } }.toSet()
        previews[needed]?.let { return it }
        val loaded = mutableState.value.skills
        val items = coroutineScope {
            interactor.classSkills(combo.classKey, stance = null).filterKeys { it in needed }.map { (no, skill) ->
                async {
                    val icon = loaded[no]?.icon ?: interactor.icon(skill)?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
                    no to SkillItem(no, skill, icon)
                }
            }.awaitAll().toMap()
        }
        return items.also { previews[needed] = it }
    }

    /** Действие игрока. */
    fun onEvent(event: ComboEvent) {
        when (event) {
            is ComboEvent.SelectCombo -> selectCombo(event.id)
            ComboEvent.NewCombo -> mutableState.update { s ->
                // новое комбо сразу выбирает свою пустую ячейку: первое умение из списка ляжет в неё
                editor.refresh(s.copy(combo = interactor.newCombo(s.characterClass.key, ""), selection = CellAt(0), dirty = true, editingRage = false), skills)
            }
            is ComboEvent.Rename -> mutableState.update { s -> s.copy(combo = s.combo?.copy(name = event.name), dirty = true) }
            is ComboEvent.SelectClass -> {
                // комбо привязано к классу: другой класс — его первое комбо, а если комбо нет — новое пустое
                val characterClass = event.characterClass
                mutableState.update { s ->
                    val first = s.combos.firstOrNull { it.classKey == characterClass.key }
                    val combo = first ?: interactor.newCombo(characterClass.key, "")
                    // у классов Освобождения стоек нет — показываются все навыки
                    val stance = ComboStance.keep(characterClass, s.stance)
                    val selection = if (first == null) CellAt(0) else null
                    editor.refresh(s.copy(characterClass = characterClass, stance = stance, combo = combo, selection = selection, dirty = first == null, editingRage = false), skills)
                }
                reloadSkills()
            }
            is ComboEvent.SelectStance -> { mutableState.update { it.copy(stance = event.stance) }; reloadSkills() }
            is ComboEvent.Query -> mutableState.update { it.copy(query = event.text) }
            ComboEvent.Save -> save()
            ComboEvent.AskDelete -> mutableState.update { it.copy(deleteAsked = it.combo != null) }
            ComboEvent.CancelDelete -> mutableState.update { it.copy(deleteAsked = false) }
            ComboEvent.Delete -> { mutableState.update { it.copy(deleteAsked = false) }; delete() }
            ComboEvent.Undo -> undo()
            ComboEvent.Redo -> redo()
            ComboEvent.ExportCombo -> mutableState.value.combo?.let { combo -> viewModelScope.launch { interactor.export(combo) } }
            ComboEvent.ImportCombos -> importCombos()
            is ComboEvent.SetMode -> setMode(event.mode)
            is ComboEvent.Press -> trainingStep { training.press(event.skillNo) }
            ComboEvent.ToggleSchema -> updateSettings { it.copy(schemaShown = !it.schemaShown) }
            ComboEvent.ShowTrackerNotice -> mutableState.update { it.copy(trackerNotice = true) }
            ComboEvent.CloseTrackerNotice -> {
                mutableState.update { it.copy(trackerNotice = false) }
                if (!mutableState.value.settings.trackerNoticeSeen) updateSettings { it.copy(trackerNoticeSeen = true) }
            }
            is ComboEvent.ShowCombo -> showCombo(event.id)
            ComboEvent.ToggleSlotTooltips -> updateSettings { it.copy(slotTooltips = !it.slotTooltips) }
            ComboEvent.ToggleSlotShields -> updateSettings { it.copy(slotShields = !it.slotShields) }.also { loadSlotProtections() }
            ComboEvent.ToggleSlotControls -> updateSettings { it.copy(slotControls = !it.slotControls) }.also { loadSlotProtections() }
            is ComboEvent.SetComboScale -> updateSettings(event.save) { it.copy(comboScale = event.percent) }
            is ComboEvent.SetComboOpacity -> updateSettings(event.save) { it.copy(comboOpacity = event.percent) }
            is ComboEvent.SetResetSeconds -> {
                updateSettings(event.save) { it.copy(resetSeconds = event.seconds) }
                if (event.save) restartTraining()
            }
            is ComboEvent.SetOverlayStyle -> changeOverlayLook { it.copy(overlayStyle = event.style) }
            ComboEvent.ToggleVertical -> changeOverlayLook { it.copy(vertical = !it.vertical) }
            ComboEvent.HideCombo -> {
                mutableState.update { it.copy(overlayShown = false, rageActive = false) }
                stopWatching()
            }
            is ComboEvent.EditRage -> mutableState.update { editor.refresh(it.copy(editingRage = event.rage, selection = null), skills) }
            ComboEvent.AddRage -> changeRage(ComboRage.Empty, editing = true)
            ComboEvent.RemoveRage -> changeRage(null, editing = false)
            else -> editCombo(event)
        }
    }

    /** Меняет настройки оверлея; сохраняет, если [save] (ползунок сохраняется, когда его отпустили). */
    private fun updateSettings(save: Boolean = true, change: (OverlaySettings) -> OverlaySettings) {
        mutableState.update { it.copy(settings = change(it.settings)) }
        val settings = mutableState.value.settings
        if (save) viewModelScope.launch { interactor.saveSettings(settings) }
    }

    /** Правка в редакторе; если поменялись шаги комбо — трекер оверлея сразу переходит на новое комбо. */
    private fun editCombo(event: ComboEvent) {
        val before = mutableState.value.combo
        mutableState.update { editor.reduce(it, event, skills) }
        val after = mutableState.value.combo ?: return
        if (before != null && before != after) rememberEdit(before)
        val changed = before?.parts()?.map { listOf(it.stepsOnly(), it.fillers, it.pool, it.keyTracked) } != after.parts().map { listOf(it.stepsOnly(), it.fillers, it.pool, it.keyTracked) }
        if (!changed) return
        if (watcher?.isActive == true) restartWatching() else restartTraining()
    }

    /** Запоминает комбо [before] до правки для Ctrl+Z; старые правки сверх [HISTORY_LIMIT] забываются, отменённые — тоже. */
    private fun rememberEdit(before: Combo) {
        history.addLast(before)
        if (history.size > HISTORY_LIMIT) history.removeFirst()
        future.clear()
        publishHistory()
    }

    /**
     * Ctrl+Z: открытое комбо — каким оно было до последней своей правки. Правки других комбо (открывали их раньше)
     * пропускаются: отмена не должна подменять одно комбо другим.
     */
    private fun undo() = step(from = history, to = future)

    /** Ctrl+Y: открытое комбо — каким оно было до последней своей отмены. */
    private fun redo() = step(from = future, to = history)

    /** Отмена или повтор: последнее комбо из [from] с тем же id становится открытым, открытое уходит в [to]. */
    private fun step(from: ArrayDeque<Combo>, to: ArrayDeque<Combo>) {
        val current = mutableState.value.combo ?: return
        while (from.isNotEmpty() && from.last().id != current.id) from.removeLast()
        val next = from.removeLastOrNull() ?: return
        to.addLast(current)
        mutableState.update { s ->
            editor.refresh(s.copy(combo = next, selection = null, dirty = true, editingRage = s.editingRage && next.rage != null), skills)
        }
        publishHistory()
        if (watcher?.isActive == true) restartWatching() else restartTraining()
    }

    /** Есть ли что отменить и повторить у открытого комбо — для кнопок над редактором. */
    private fun publishHistory() = mutableState.update { s ->
        val id = s.combo?.id
        s.copy(canUndo = history.any { it.id == id }, canRedo = future.any { it.id == id })
    }

    /** Добавляет или убирает ([rage] = `null`) комбо под гневом; [editing] — править ли его дальше. */
    private fun changeRage(rage: ComboRage?, editing: Boolean) {
        mutableState.value.combo?.let(::rememberEdit)
        mutableState.update { s ->
            val combo = s.combo?.copy(rage = rage) ?: return@update s
            editor.refresh(s.copy(combo = combo, editingRage = editing, selection = if (editing) CellAt(0) else null, dirty = true), skills)
        }
        if (watcher?.isActive == true) restartWatching() else restartTraining()
    }

    /**
     * Вид оверлея открытого комбо ([change]: стиль, вертикальность): сохраняется сразу, вместе с сохранённой версией
     * комбо, не трогая несохранённые правки.
     */
    private fun changeOverlayLook(change: (Combo) -> Combo) {
        val id = mutableState.value.combo?.id ?: return
        mutableState.update { s -> s.copy(combo = s.combo?.let(change), combos = s.combos.map { if (it.id == id) change(it) else it }) }
        mutableState.value.combos.firstOrNull { it.id == id }?.let { viewModelScope.launch { interactor.save(it) } }
    }

    /**
     * Следит, каким персонажем играют: его слоты — для тултипов, а если сменился класс и для него есть комбо —
     * открывает первое такое комбо (оверлей и слежение переходят на него).
     */
    private fun followPlayedCharacter() = viewModelScope.launch {
        while (isActive) {
            interactor.playedCharacter()?.let { played ->
                mutableState.update { it.copy(slots = played.slots, playedClass = played.classKey ?: it.playedClass) }
                loadSlotProtections()
                val s = mutableState.value
                if (played.classKey != null && played.classKey != s.combo?.classKey && !s.dirty) {
                    s.combos.firstOrNull { it.classKey == played.classKey }?.let { selectCombo(it.id) }
                }
            }
            delay(CHARACTER_CHECK_MS)
        }
    }

    /**
     * Защита и контроли умений в слотах для значков на них: только пока щиты или контроли включены и только тех
     * умений, чьих данных ещё нет.
     * Не скачалось — попробуем при следующей проверке слотов.
     */
    private fun loadSlotProtections() {
        val s = mutableState.value
        if (!s.settings.slotShields && !s.settings.slotControls) return
        s.slots.map { it.skillNo }.distinct().filter { it !in s.slotProtections }.forEach { skillNo ->
            viewModelScope.launch {
                val traits = interactor.skillTraits(skillNo) ?: return@launch
                mutableState.update {
                    it.copy(slotProtections = it.slotProtections + (skillNo to traits.protections), slotControls = it.slotControls + (skillNo to traits.controls))
                }
            }
        }
    }

    /** Комбо [id] в оверлей; слежение за ним перезапускается само, когда загрузятся умения его класса ([loadSkills]). */
    private fun showCombo(id: String) {
        // до выбора: выбор перезапускает слежение, а оно идёт, только пока оверлей показан
        mutableState.update { it.copy(overlayShown = true) }
        selectCombo(id)
        ensureTicker()
    }

    private fun classOf(combo: Combo): CharacterClass? =
        CharacterClass.entries.firstOrNull { it.key == combo.classKey }

    private fun selectCombo(id: String) = mutableState.update { s ->
        val combo = s.combos.firstOrNull { it.id == id } ?: return@update s
        val characterClass = classOf(combo) ?: s.characterClass
        val stance = ComboStance.keep(characterClass, s.stance)
        editor.refresh(s.copy(combo = combo, selection = null, dirty = false, characterClass = characterClass, stance = stance, editingRage = false), skills)
    }.also { publishHistory(); reloadSkills() }

    /** Перезагружает умения класса; прошлая загрузка отменяется, чтобы её иконки не попали в список нового класса. */
    private fun reloadSkills() {
        skillsLoader?.cancel()
        skillsLoader = viewModelScope.launch { loadSkills() }
    }

    private suspend fun loadSkills() = coroutineScope {
        val s = mutableState.value
        mutableState.update { it.copy(loading = true) }
        val classSkills = interactor.classSkills(s.characterClass.key, s.stance)
        skills = interactor.comboSkills(classSkills)
        val known = s.skills
        mutableState.update { st ->
            val items = classSkills.mapValues { (no, skill) -> SkillItem(no, skill, known[no]?.icon, known[no]?.traits) }
            editor.refresh(st.copy(loading = false, catalogFailed = classSkills.isEmpty(), skills = items), skills)
        }
        if (s.mode == ComboMode.Overlay) restartTraining()
        // в настольной версии программа следит за игрой, пока оверлей комбо показан — за открытым комбо, с его умениями
        if (interactor.canWatchGame) restartWatching()
        // иконки и урон — параллельно: по одной загрузка класса шла десятки секунд
        classSkills.forEach { (no, skill) ->
            if (known[no]?.icon == null) launch {
                val icon = interactor.icon(skill)?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() } ?: return@launch
                updateItem(no) { it.copy(icon = icon) }
            }
            if (known[no]?.traits == null) launch { interactor.skillTraits(no)?.let { traits -> updateItem(no) { it.copy(traits = traits) } } }
        }
    }

    /** Меняет умение [skillNo] в списке, если оно ещё в нём (класс могли сменить). */
    private fun updateItem(skillNo: Int, change: (SkillItem) -> SkillItem) = mutableState.update { st ->
        st.skills[skillNo]?.let { st.copy(skills = st.skills + (skillNo to change(it))) } ?: st
    }

    private fun save() = viewModelScope.launch {
        val combo = mutableState.value.combo ?: return@launch
        interactor.save(combo)
        val combos = interactor.combos()
        mutableState.update { it.copy(combos = combos, dirty = false) }
    }

    /** Комбо из файла — в сохранённые, и первое из них открывается; не файл комбо — предупреждение в меню. */
    private fun importCombos() = viewModelScope.launch {
        val imported = try {
            interactor.import()
        } catch (e: IllegalArgumentException) {
            mutableState.update { it.copy(importFailed = true) }
            return@launch
        } ?: return@launch
        val combos = interactor.combos()
        mutableState.update { it.copy(combos = combos, importFailed = false) }
        imported.firstOrNull()?.let { selectCombo(it.id) }
    }

    /**
     * Удаляет открытое комбо. Дальше — другое комбо того же класса, а если его нет — новое пустое комбо этого класса:
     * чужое комбо со старым списком умений показалось бы пустыми ячейками.
     */
    private fun delete() = viewModelScope.launch {
        val combo = mutableState.value.combo ?: return@launch
        interactor.delete(combo.id)
        val combos = interactor.combos()
        mutableState.update { it.copy(combos = combos) }
        val next = combos.firstOrNull { it.classKey == combo.classKey }
        if (next != null) {
            selectCombo(next.id)
        } else {
            mutableState.update { s -> editor.refresh(s.copy(combo = interactor.newCombo(combo.classKey, ""), selection = CellAt(0), dirty = true), skills) }
            if (interactor.canWatchGame) restartWatching()
        }
    }

    private fun setMode(mode: ComboMode) {
        mutableState.update { it.copy(mode = mode) }
        if (mode == ComboMode.Overlay) restartTraining()
        ensureTicker()
    }

    private fun restartTraining() {
        val combo = mutableState.value.tracked ?: return
        training.load(combo, skills, TrackerSettings(idleMs = mutableState.value.settings.resetSeconds * MS_PER_SECOND))
        publishTraining()
    }

    private fun trainingStep(action: () -> Unit) { action(); publishTraining() }

    private fun publishTraining() {
        mutableState.update { it.copy(overlay = training.snapshot()) }
        keySteps = training.keySteps(keyCommands)
    }

    /** Пока идёт тренировка, слежение или открыт оверлей — трекер проверяет время и обновляет таймеры. */
    private fun ensureTicker() {
        val s = mutableState.value
        val needed = s.mode == ComboMode.Overlay || s.watching || s.overlayShown
        if (!needed) { ticker?.cancel(); ticker = null; return }
        if (ticker?.isActive == true) return
        ticker = viewModelScope.launch {
            while (isActive) {
                training.tick()
                publishTraining()
                delay(TICK_MS)
            }
        }
    }

    /** Перезапускает слежение за игрой; пока оверлей комбо скрыт, экран, бафы, ресурс и клавиши не читаются. */
    private fun restartWatching() {
        stopWatching()
        if (mutableState.value.combo != null && mutableState.value.overlayShown) startWatching()
    }

    private fun stopWatching() {
        watcher?.cancel()
        watcher = null
        mutableState.update { it.copy(watching = false) }
        ensureTicker()
    }

    private fun startWatching() {
        restartTraining()
        mutableState.update { it.copy(watching = true) }
        ensureTicker()
        watcher = viewModelScope.launch {
            val icons = mutableState.value.skills.values.mapNotNull { item -> interactor.icon(item.skill)?.let { item.skillNo to it } }.toMap()
            val combo = mutableState.value.combo
            val preferred = combo?.parts().orEmpty().flatMap { c -> c.cells().mapNotNull { it.second.skillNo } + c.fillers + c.pool }.toSet()
            keyCommands = interactor.keyCommands(preferred)
            // комбо под гневом есть — трекер переходит на него, пока висят бафы гнева, и обратно, когда они спали
            if (combo?.rage != null) launch {
                interactor.rageActive().collect { active ->
                    if (active == mutableState.value.rageActive) return@collect
                    mutableState.update { it.copy(rageActive = active) }
                    restartTraining()
                }
            } else mutableState.update { it.copy(rageActive = false) }
            interactor.watchGame(icons, preferred) { keySteps }.collect { skillNo -> trainingStep { training.fromGame(skillNo) } }
        }
    }

    /** Ячейки комбо без пометок: пометка шагов не меняет, и трекер из-за неё не начинается заново. */
    private fun Combo.stepsOnly() = columns.map { column -> column.lanes.map { lane -> lane.map { it.copy(note = "") } } }

    private companion object {
        const val TICK_MS = 100L

        /** Сколько правок помнит Ctrl+Z. */
        const val HISTORY_LIMIT = 100

        const val MS_PER_SECOND = 1000L

        /** Как часто узнавать, каким персонажем играют. */
        const val CHARACTER_CHECK_MS = 5000L
    }
}
