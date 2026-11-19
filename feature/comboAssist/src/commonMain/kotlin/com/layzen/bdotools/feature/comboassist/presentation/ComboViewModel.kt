package com.layzen.bdotools.feature.comboassist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboInteractor
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.presentation.controller.EditorController
import com.layzen.bdotools.feature.comboassist.presentation.controller.TrainingController
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboMode
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
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
    private var skillsLoader: Job? = null

    /** Состояние экрана. */
    val state: StateFlow<ComboState> = mutableState.asStateFlow()

    /** Видны ли слоты перезарядок на экране игры; проверяется, только пока на это подписаны (щиты включены). */
    val slotsShown: StateFlow<Boolean> = interactor.slotsShown().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    /** Может ли платформа следить за игрой и показывать оверлей поверх неё. */
    val canWatchGame: Boolean get() = interactor.canWatchGame

    init {
        viewModelScope.launch {
            val combos = interactor.combos()
            val first = combos.firstOrNull()
            val settings = interactor.settings()
            mutableState.update { it.copy(combos = combos, combo = first, settings = settings, characterClass = first?.let(::classOf) ?: it.characterClass) }
            reloadSkills()
            if (interactor.canWatchGame) followPlayedCharacter()
        }
    }

    /** Подробности умения для тултипа и клавиш; справочник кэширует их сам. */
    suspend fun details(skillNo: Int): SkillDetails? = interactor.details(skillNo)

    /** Действие игрока. */
    fun onEvent(event: ComboEvent) {
        when (event) {
            is ComboEvent.SelectCombo -> selectCombo(event.id)
            ComboEvent.NewCombo -> mutableState.update { s ->
                // новое комбо сразу выбирает свою пустую ячейку: первое умение из списка ляжет в неё
                editor.refresh(s.copy(combo = interactor.newCombo(s.characterClass.key, ""), selection = CellAt(0), dirty = true), skills)
            }
            is ComboEvent.Rename -> mutableState.update { s -> s.copy(combo = s.combo?.copy(name = event.name), dirty = true) }
            is ComboEvent.SelectClass -> {
                // комбо привязано к классу: другой класс — его первое комбо, а если комбо нет — новое пустое
                val characterClass = event.characterClass
                mutableState.update { s ->
                    val first = s.combos.firstOrNull { it.classKey == characterClass.key }
                    val combo = first ?: interactor.newCombo(characterClass.key, "")
                    // у классов Освобождения стоек нет — показываются все навыки
                    val stance = if (characterClass.ascension) null else s.stance
                    val selection = if (first == null) CellAt(0) else null
                    editor.refresh(s.copy(characterClass = characterClass, stance = stance, combo = combo, selection = selection, dirty = first == null), skills)
                }
                reloadSkills()
            }
            is ComboEvent.SelectStance -> { mutableState.update { it.copy(stance = event.stance) }; reloadSkills() }
            is ComboEvent.Query -> mutableState.update { it.copy(query = event.text) }
            ComboEvent.Save -> save()
            ComboEvent.Delete -> delete()
            ComboEvent.ExportCombo -> mutableState.value.combo?.let { combo -> viewModelScope.launch { interactor.export(combo) } }
            ComboEvent.ImportCombos -> importCombos()
            is ComboEvent.SetMode -> setMode(event.mode)
            is ComboEvent.Press -> trainingStep { training.press(event.skillNo) }
            ComboEvent.ToggleSchema -> updateSettings { it.copy(schemaShown = !it.schemaShown) }
            is ComboEvent.ShowCombo -> showCombo(event.id)
            ComboEvent.ToggleSlotTooltips -> updateSettings { it.copy(slotTooltips = !it.slotTooltips) }
            ComboEvent.ToggleSlotShields -> updateSettings { it.copy(slotShields = !it.slotShields) }.also { loadSlotProtections() }
            ComboEvent.ToggleSlotControls -> updateSettings { it.copy(slotControls = !it.slotControls) }.also { loadSlotProtections() }
            is ComboEvent.SetComboScale -> updateSettings(event.save) { it.copy(comboScale = event.percent) }
            is ComboEvent.SetComboOpacity -> updateSettings(event.save) { it.copy(comboOpacity = event.percent) }
            is ComboEvent.SetOverlayStyle -> changeOverlayLook { it.copy(overlayStyle = event.style) }
            ComboEvent.ToggleVertical -> changeOverlayLook { it.copy(vertical = !it.vertical) }
            ComboEvent.HideCombo -> mutableState.update { it.copy(overlayShown = false) }
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
        val changed = before?.stepsOnly() != after.stepsOnly() || before.fillers != after.fillers
        if (!changed) return
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
        selectCombo(id)
        mutableState.update { it.copy(overlayShown = true) }
        ensureTicker()
    }

    private fun classOf(combo: Combo): CharacterClass? =
        CharacterClass.entries.firstOrNull { it.key == combo.classKey }

    private fun selectCombo(id: String) = mutableState.update { s ->
        val combo = s.combos.firstOrNull { it.id == id } ?: return@update s
        val characterClass = classOf(combo) ?: s.characterClass
        val stance = if (characterClass.ascension) null else s.stance
        editor.refresh(s.copy(combo = combo, selection = null, dirty = false, characterClass = characterClass, stance = stance), skills)
    }.also { reloadSkills() }

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
        // в настольной версии программа следит за игрой всегда — за открытым комбо, с его умениями
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
        val combo = mutableState.value.combo ?: return
        training.load(combo, skills)
        publishTraining()
    }

    private fun trainingStep(action: () -> Unit) { action(); publishTraining() }

    private fun publishTraining() = mutableState.update { it.copy(overlay = training.snapshot()) }

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

    private fun restartWatching() {
        stopWatching()
        if (mutableState.value.combo != null) startWatching()
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
            val preferred = combo?.let { c -> c.cells().mapNotNull { it.second.skillNo }.toSet() + c.fillers }.orEmpty()
            interactor.watchGame(icons, preferred).collect { skillNo -> trainingStep { training.fromGame(skillNo) } }
        }
    }

    /** Ячейки комбо без пометок: пометка шагов не меняет, и трекер из-за неё не начинается заново. */
    private fun Combo.stepsOnly() = columns.map { column -> column.lanes.map { lane -> lane.map { it.copy(note = "") } } }

    private companion object {
        const val TICK_MS = 100L

        /** Как часто узнавать, каким персонажем играют. */
        const val CHARACTER_CHECK_MS = 5000L
    }
}
