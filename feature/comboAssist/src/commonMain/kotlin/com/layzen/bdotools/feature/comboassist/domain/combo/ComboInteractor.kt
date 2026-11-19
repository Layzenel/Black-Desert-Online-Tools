package com.layzen.bdotools.feature.comboassist.domain.combo

import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.core.skill.domain.Skill
import com.layzen.bdotools.core.skill.domain.SkillCatalogRepository
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.core.skill.domain.SkillStance
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkill
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillTraits
import com.layzen.bdotools.feature.comboassist.domain.skill.skillProtections
import com.layzen.bdotools.feature.comboassist.domain.history.PlayedCharacter
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import com.layzen.bdotools.feature.comboassist.domain.history.SkillHistorySource
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

    /** Видны ли слоты перезарядок на экране: щиты на них прячутся, когда слоты закрыты окном игры. */
    fun slotsShown(): Flow<Boolean> = history.slotsShown()

    /** Применённые в игре умения по их иконкам [icons], пока поток собирают; [preferred] — умения комбо. */
    fun watchGame(icons: Map<Int, ByteArray>, preferred: Set<Int>): Flow<Int> = history.watch(icons, preferred)

    /**
     * Персонаж, которым сейчас играют, по слотам перезарядок на экране, или `null`, если не понять. Класс — тот,
     * чьих умений в слотах больше всего (общие умения нескольких классов не решают).
     */
    suspend fun playedCharacter(): PlayedCharacter? {
        val skills = catalog.activeSkills()?.skills ?: return null
        val slots = history.playedSlots { no -> skills[no]?.let { catalog.icon(it.icon) } } ?: return null
        val classKey = slots.mapNotNull { skills[it.skillNo]?.classes?.singleOrNull() }
            .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return PlayedCharacter(slots, classKey)
    }

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

    /** Новое пустое комбо класса [classKey] с одной пустой колонкой. */
    fun newCombo(classKey: String, name: String): Combo =
        Combo(id = newId(), name = name, classKey = classKey, columns = listOf(ComboColumn.Single(ComboCell())))

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
    suspend fun classSkills(classKey: String, stance: SkillStance?): Map<Int, Skill> {
        val skills = catalog.activeSkills()?.skills.orEmpty().filterValues { skill ->
            skill.max && classKey in skill.classes && (stance == null || skill.fitsStance(stance)) &&
                !(stance == SkillStance.Succession && skill.hasPrimeVariant)
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
