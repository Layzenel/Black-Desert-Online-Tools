package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import com.layzen.bdotools.feature.uieditor.domain.skill.Skill
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillsInteractor
import com.layzen.bdotools.feature.uieditor.domain.skill.cooldownSkills
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.thumbnail
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Навыки персонажей в слотах перезарядки: список персонажей аккаунта с портретами, выбор одного из них, справочник
 * и иконки. Всё грузится в фоне и страницу не блокирует: без сети редактор работает, только без названий и иконок.
 */
internal class SkillsController(
    private val context: SlotsContext,
    private val interactor: SkillsInteractor,
    private val accounts: GameAccounts,
) {
    /** Навыки, подробности которых качаются сейчас: наведение на тот же слот не запускает загрузку повторно. */
    private val detailsLoading = mutableSetOf<Int>()

    /**
     * Читает навыки персонажей аккаунта [account], их подписи из «Конструктора портретов» и портреты. Выбран остаётся тот же персонаж, если он
     * есть в аккаунте; в новом аккаунте персонаж не выбран, пока его не выберут.
     */
    fun load(account: String) = context.launch {
        val characters = readCharacters(account)
        val labels = accounts.readCharacterLabels(account)
        // Пока файлы читались, открыли другой аккаунт: его навыки грузит своя загрузка
        if (context.current.loaded?.account != account) return@launch
        val selected = context.current.skills.selected?.takeIf { selected -> characters.any { it.characterNo == selected } }
        context.update {
            it.copy(skills = it.skills.copy(characters = characters, labels = labels, portraits = emptyMap()))
        }
        show(selected)
        loadCatalog()
        loadIcons()
        characters.forEach { character -> context.launch { loadPortrait(character.characterNo) } }
    }

    /**
     * Записывает выбранному персонажу навыки и надписи редактора, если они отличаются от его файла, и перечитывает
     * файлы персонажей: навыки в редакторе остаются, меняется только то, с чем их сравнивать.
     */
    suspend fun write(account: String) {
        val state = context.current
        val targets = state.skills.skillTargets(state.skillsChanged)
        if (targets.isEmpty()) return
        targets.forEach { character ->
            // Не заданное в раскладке (`null`) остаётся как в файле персонажа
            val skills = state.layout.skills ?: character.cooldownSkills
            interactor.write(account, character.characterNo, skills, state.layout.memos ?: character.memos)
        }
        val characters = readCharacters(account)
        updateSkills { it.copy(characters = characters, alsoApplyTo = emptySet()) }
    }

    /** Кроме выбранного персонажа «Применить» запишет навыки редактора ещё и персонажам [characterNos]. */
    fun alsoApplyTo(characterNos: Set<String>) = updateSkills { it.copy(alsoApplyTo = characterNos.filterNot { no -> no == it.selected }.toSet()) }

    /**
     * Навыки и надписи персонажа [characterNo] — в редактор; `null` — без навыков. Правки навыков прежнего персонажа
     * пропадают, в том числе из «Отменить»: они относились к нему.
     */
    fun select(characterNo: String?) {
        show(characterNo)
        context.launch { loadIcons() }
    }

    /**
     * Скачивает подробности навыка [skillNo] для тултипа, если их ещё нет и они уже не качаются. Не скачались —
     * навык попадает в [SkillsState.detailsFailed], следующий запрос пробует снова.
     */
    fun loadDetails(skillNo: Int) {
        if (skillNo in context.current.skills.details || !detailsLoading.add(skillNo)) return
        updateSkills { it.copy(detailsFailed = it.detailsFailed - skillNo) }
        context.launch {
            try {
                val details = interactor.details(skillNo)
                updateSkills {
                    if (details == null) it.copy(detailsFailed = it.detailsFailed + skillNo)
                    else it.copy(details = it.details + (skillNo to details))
                }
            } finally {
                detailsLoading -= skillNo
            }
        }
    }

    /** Докачивает иконки навыков [skillNos] (по умолчанию — тех, что в слотах редактора), которых ещё нет. */
    fun loadIcons(skillNos: Collection<Int> = context.current.layout.skills?.values.orEmpty()) {
        val skills = context.current.skills
        val missing = skillNos.mapNotNull { skills.skill(it) }.filter { it.icon !in skills.icons }.distinctBy { it.icon }
        missing.forEach { skill -> context.launch { loadIcon(skill) } }
    }

    private fun show(characterNo: String?) = context.update { state ->
        val character = state.skills.characters.firstOrNull { it.characterNo == characterNo }
        state.copy(
            skills = state.skills.copy(selected = character?.characterNo, alsoApplyTo = emptySet()),
            layout = state.layout.withCharacterSkills(character),
            history = state.history.withCharacter(character),
            future = state.future.withCharacter(character),
        )
    }

    private fun List<SlotLayout>.withCharacter(character: CharacterSkills?) = map { it.withCharacterSkills(character) }

    private suspend fun readCharacters(account: String): List<CharacterSkills> {
        val order = accounts.state.value.selected?.takeIf { it.id == account }?.characterIds.orEmpty()
        return interactor.characters(account, order)
    }

    private suspend fun loadCatalog() {
        if (context.current.skills.catalog != null) return
        val catalog = interactor.catalog()
        context.update { it.copy(skills = it.skills.copy(catalog = catalog, catalogFailed = catalog == null)) }
    }

    private suspend fun loadIcon(skill: Skill) {
        val image = interactor.icon(skill)?.let(::decodeOrNull) ?: return
        context.update { it.copy(skills = it.skills.copy(icons = it.skills.icons + (skill.icon to image))) }
    }

    // Портрет необязателен: нет файла или он битый — у персонажа пустая ячейка
    private suspend fun loadPortrait(characterNo: String) {
        val image = accounts.readPortrait(characterNo)?.let(::decodeOrNull)?.thumbnail() ?: return
        context.update { it.copy(skills = it.skills.copy(portraits = it.skills.portraits + (characterNo to image))) }
    }

    private fun decodeOrNull(bytes: ByteArray) = try {
        bytes.decodeToImageBitmap()
    } catch (e: Exception) {
        null
    }

    private fun updateSkills(change: (SkillsState) -> SkillsState) = context.update { it.copy(skills = change(it.skills)) }
}
