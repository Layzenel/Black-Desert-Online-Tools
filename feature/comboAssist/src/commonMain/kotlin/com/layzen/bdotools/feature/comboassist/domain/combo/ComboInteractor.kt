package com.layzen.bdotools.feature.comboassist.domain.combo

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.core.skill.domain.Skill
import com.layzen.bdotools.core.skill.domain.SkillCatalogRepository
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboStance
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkill
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillTraits
import com.layzen.bdotools.feature.comboassist.domain.skill.skillProtections
import com.layzen.bdotools.feature.comboassist.domain.history.PlayedCharacter
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import com.layzen.bdotools.feature.comboassist.domain.history.SkillHistorySource
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyStep
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyCommand
import com.layzen.bdotools.feature.comboassist.domain.keys.keyVariants
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

/**
 * Сценарии редактора комбо: список комбо, сохранение, умения класса с иконками и подробностями.
 *
 * @property combos хранилище комбо
 * @property catalog справочник навыков
 * @property history применённые умения из игры
 * @property files экспорт и импорт комбо в файл
 */
class ComboInteractor(
    private val combos: ComboRepository,
    private val catalog: SkillCatalogRepository,
    private val history: SkillHistorySource,
    private val files: ComboFiles,
) {
    /** Может ли программа следить за историей умений в игре (только настольная версия). */
    val canWatchGame: Boolean get() = history.supported

    /** Какие слоты перезарядок закрыты окном игры: оверлей на них не рисуется, а закрыты все — трекер прячется. */
    fun hiddenSlots(): Flow<Set<Int>> = history.hiddenSlots()

    /** Висят ли бафы гнева Чёрного духа, пока поток собирают: тогда трекер ведёт комбо под гневом. */
    fun rageActive(): Flow<Boolean> = history.rageActive()

    /**
     * Применённые в игре умения по их иконкам [icons], пока поток собирают; [preferred] — умения комбо, [keySteps] — шаги, которых трекер
     * ждёт сейчас (их подтверждают клавиши).
     */
    fun watchGame(
        icons: Map<Int, ByteArray>,
        preferred: Set<Int>,
        keySteps: () -> List<KeyStep>,
    ): Flow<Int> =
        history.watch(icons, preferred, keySteps)

    /**
     * Команды умений [skillNos]: варианты клавиш ([keyVariants]) и заметен ли его расход ресурса класса (MP в данных bdolytics).
     * Умения без подробностей или клавиш пропускаются.
     */
    suspend fun keyCommands(skillNos: Set<Int>): Map<Int, KeyCommand> = coroutineScope {
        skillNos.map { no -> async { no to catalog.details(no)?.text(COOLDOWN_LANGUAGE) } }
            .awaitAll()
            .mapNotNull { (no, text) ->
                val variants = text?.command?.let(::keyVariants)?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                no to KeyCommand(variants, visibleSpend = text.requiredMp >= VISIBLE_SPEND)
            }
            .toMap()
    }

    /**
     * Персонаж, которым сейчас играют, по слотам перезарядок на экране, или `null`, если не понять. Класс — тот,
     * чьих умений в слотах больше всего (общие умения нескольких классов не решают).
     */
    suspend fun playedCharacter(): PlayedCharacter? {
        val skills = catalog.activeSkills()?.skills ?: return null
        val slots = history.playedSlots { no -> skills[no]?.let { catalog.icon(it.icon) } } ?: return null
        val classKey = slots.mapNotNull { skills[it.skillNo]?.classes?.singleOrNull() }
            .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return PlayedCharacter(slots, classKey, history.historyShown())
    }

    /** Умение [skillNo] из справочника любого класса или `null`, если справочник не скачался или такого нет. */
    suspend fun skill(skillNo: Int): Skill? = catalog.activeSkills()?.skills?.get(skillNo)

    /** Сохранённые комбо. */
    suspend fun combos(): List<Combo> = combos.combos()

    /** Сохраняет комбо. */
    suspend fun save(combo: Combo) = combos.save(combo)

    /** Удаляет комбо. */
    suspend fun delete(id: String) = combos.delete(id)

    /** Настройки оверлея. */
    suspend fun settings(): OverlaySettings = combos.settings()

    /** Сохраняет настройки оверлея. */
    suspend fun saveSettings(settings: OverlaySettings) = combos.saveSettings(settings)

    /**
     * Новое пустое комбо класса [classKey] с одной пустой колонкой. У Серафима комбо под гневом Чёрного духа другое —
     * его пустой вариант добавляется сразу.
     */
    fun newCombo(classKey: String, name: String): Combo = Combo(
        id = newId(), name = name, classKey = classKey, columns = listOf(ComboColumn.Single(ComboCell())),
        rage = ComboRage.Empty.takeIf { classKey == CharacterClass.Seraph.key },
    )

    /** Сохраняет [combo] в файл, который выберет игрок. */
    suspend fun export(combo: Combo) = files.export(combo)

    /**
     * Комбо из файла, который выберет игрок, — сразу в сохранённые; `null`, если окно закрыли. Комбо с номером,
     * который уже есть, получает новый номер: импорт не затирает своё комбо.
     *
     * @throws IllegalArgumentException если это не файл комбо
     */
    suspend fun import(): List<Combo>? {
        val imported = files.import() ?: return null
        val taken = combos().mapTo(mutableSetOf()) { it.id }
        return imported.map { combo -> (if (combo.id in taken) combo.copy(id = newId()) else combo).also { save(it) } }
    }

    private fun newId(): String = Random.nextLong().toULong().toString(36)

    /**
     * Умения класса [classKey], которые можно поставить в комбо персонажу в стойке [stance] (`null` — все):
     * последние ранги, с перезарядкой и без. Пусто, если справочник не скачался.
     */
    suspend fun classSkills(classKey: String, stance: ComboStance?): Map<Int, Skill> {
        val skills = catalog.activeSkills()?.skills.orEmpty().filterValues { skill ->
            skill.max && classKey in skill.classes && (stance == null || stance.allows(skill))
        }
        return withCooldowns(skills)
    }

    /**
     * Перезарядка из подробностей умения, если в справочнике её нет (старый справочник без поля `cooldown`).
     * Подробности скачиваются параллельно и кэшируются справочником; умения без подробностей остаются как есть.
     */
    private suspend fun withCooldowns(skills: Map<Int, Skill>): Map<Int, Skill> = coroutineScope {
        val missing = skills.filterValues { it.cooldownMs == 0L }.keys
        val found = missing.map { no -> async { no to catalog.details(no)?.text(COOLDOWN_LANGUAGE)?.cooldownMs } }.awaitAll()
        skills + found.mapNotNull { (no, cooldown) -> cooldown?.takeIf { it > 0 }?.let { no to skills.getValue(no).copy(cooldownMs = it) } }
    }

    /**
     * Урон, клавиши и защита умения по английскому тултипу: урон — есть строка урона («1325% x2», «damage … x1.15»),
     * клавиши — действия `{KeyBind:…}` из команды, защита и контроли — по строкам эффектов ([skillProtections], [CrowdControl.pvpOf]). `null` — подробности не скачались.
     */
    // shortcut: урон по тексту тултипа; если начнёт ошибаться — флаг урона в справочнике bdo-tools-data
    suspend fun skillTraits(skillNo: Int): SkillTraits? {
        val text = catalog.details(skillNo)?.text(COOLDOWN_LANGUAGE) ?: return null
        val effects = text.effects.flatten()
        val damage = effects.any { DAMAGE.containsMatchIn(it) }
        return SkillTraits(damage, KEY_BIND.findAll(text.command).map { it.groupValues[1] }.toSet(), skillProtections(effects), CrowdControl.pvpOf(text.effects.map { it.joinToString("") }))
    }

    private companion object {
        /** Перезарядка одинакова на всех языках: берётся из английского текста. */
        const val COOLDOWN_LANGUAGE = "en"

        /**
         * Расход от этого (MP в данных bdolytics) заметен на полоске ресурса — несколько пикселей; дешевле (связки Страйкера
         * по 5 из ~850) тонет в дрожании замера, и такое умение засчитывается по нажатию.
         * shortcut: порог в единицах ресурса, а не в доле полоски — у классов с большим запасом может быть мал; уточнить по `watch.log`.
         */
        const val VISIBLE_SPEND = 20

        /** Клавиша в команде умения: `{KeyBind:Attack1}`. */
        val KEY_BIND = Regex("""\{KeyBind:(\w+)\}""")

        /** Строка урона тултипа: проценты с числом ударов или «damage … x множитель», теги цвета между ними. */
        val DAMAGE = Regex("""\d%\s*x\s*\d|damage.{0,80}x\s*[\d.]+""", RegexOption.IGNORE_CASE)
    }

    /** Перезарядки и связки умений [skills] для трекера. */
    fun comboSkills(skills: Map<Int, Skill>): ComboSkills = ComboSkills(
        skills.mapValues { (no, skill) -> ComboSkill(no, skill.cooldownMs, skill.after, skill.onlyAfter) },
    )

    /** Байты иконки умения или `null`, если она не скачалась. */
    suspend fun icon(skill: Skill): ByteArray? = catalog.icon(skill.icon)

    /** Подробности умения для тултипа и клавиш или `null`. */
    suspend fun details(skillNo: Int): SkillDetails? = catalog.details(skillNo)
}
