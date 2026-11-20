package com.layzen.bdotools.feature.comboassist.presentation.state

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboStance
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings

/**
 * Состояние экрана комбо.
 *
 * @property combos сохранённые комбо
 * @property combo комбо в редакторе
 * @property dirty есть несохранённые правки
 * @property characterClass класс комбо
 * @property stance стойка, чьи умения предлагаются
 * @property skills умения класса по номеру навыка
 * @property loading справочник ещё грузится
 * @property catalogFailed справочник не скачался
 * @property query поиск в списке умений
 * @property selection выбранная ячейка
 * @property suggestions что можно поставить после выбранной ячейки (связки из данных)
 * @property problems ячейки-продолжения связок не после своего запуска
 * @property emptyCells пустых ячеек в комбо
 * @property mode редактор или тренировка
 * @property overlay снимок трекера для оверлея
 * @property watching программа следит за историей умений в игре (только настольная версия)
 * @property overlayShown окно оверлея поверх игры открыто (только настольная версия)
 * @property slots слоты перезарядок персонажа, пока идёт слежение: при наведении на них — тултип умения
 * @property slotProtections защита умений в слотах для щитов на них (загружается, только пока щиты или контроли включены)
 * @property slotControls контроли умений в слотах для значков под ними (загружаются вместе с [slotProtections])
 * @property settings настройки окон поверх игры
 * @property importFailed последний выбранный для импорта файл — не файл комбо
 * @property playedClass класс персонажа, которым играют (по слотам на экране); `null` — ещё не узнан
 * @property editingRage редактор правит комбо под гневом Чёрного духа, а не обычное
 * @property rageActive в игре висят бафы гнева Чёрного духа: трекер ведёт комбо под гневом, если оно есть
 * @property trackerNotice открыто окно «Как работает трекер» (само — при первом открытии вкладки в настольной версии)
 */
data class ComboState(
    val combos: List<Combo> = emptyList(),
    val combo: Combo? = null,
    val dirty: Boolean = false,
    val characterClass: CharacterClass = CharacterClass.Striker,
    val stance: ComboStance? = ComboStance.Succession,
    val skills: Map<Int, SkillItem> = emptyMap(),
    val loading: Boolean = true,
    val catalogFailed: Boolean = false,
    val query: String = "",
    val selection: CellAt? = null,
    val suggestions: List<Int> = emptyList(),
    val problems: List<CellAt> = emptyList(),
    val emptyCells: Int = 0,
    val mode: ComboMode = ComboMode.Editor,
    val overlay: OverlayState = OverlayState(),
    val watching: Boolean = false,
    val overlayShown: Boolean = false,
    val slots: List<ScreenSlot> = emptyList(),
    val slotProtections: Map<Int, Set<SkillProtection>> = emptyMap(),
    val slotControls: Map<Int, Set<CrowdControl>> = emptyMap(),
    val settings: OverlaySettings = OverlaySettings(),
    val importFailed: Boolean = false,
    val playedClass: String? = null,
    val editingRage: Boolean = false,
    val rageActive: Boolean = false,
    val trackerNotice: Boolean = false,
) {
    /** Редактор правит комбо под гневом: оно выбрано ([editingRage]) и есть у комбо. */
    val rageEdited: Boolean get() = editingRage && combo?.rage != null

    /** Часть комбо в редакторе: обычное комбо или комбо под гневом ([rageEdited]). */
    val edited: Combo? get() = combo?.part(rageEdited)

    /** Трекер ведёт комбо под гневом: гнев активен ([rageActive]) и такое комбо есть. */
    val rageTracked: Boolean get() = rageActive && combo?.rage != null

    /** Часть комбо, по которой идёт трекер: под гневом ([rageTracked]) или обычное. */
    val tracked: Combo? get() = combo?.part(rageTracked)
}
