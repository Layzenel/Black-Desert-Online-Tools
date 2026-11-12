package com.layzen.bdotools.feature.uieditor.presentation.state

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutError
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillStance

/** Открытое модальное окно страницы. */
sealed interface SlotsDialog {
    data class SaveAs(val name: String) : SlotsDialog

    /** Импорт: вставленный текст и ошибка последней попытки его прочитать. */
    data class Import(val text: String, val error: LayoutError? = null) : SlotsDialog

    /**
     * Превью перед импортом или экспортом: раскладка [layout] на экране [screen]. При экспорте экран можно поменять,
     * чтобы посмотреть, как раскладка ляжет на чужой монитор; при импорте это всегда экран игры.
     * Импортируются или экспортируются только элементы [included] и окна чата [includedChats]. [past] и [future] —
     * прежние и отменённые наборы отметок для «Отменить» и «Повторить».
     */
    data class Preview(
        val mode: PreviewMode,
        val layout: NamedLayout,
        val screen: ScreenSpace,
        val included: Set<SlotId>,
        val includedChats: Set<Int> = emptySet(),
        val past: List<PreviewChoice> = emptyList(),
        val future: List<PreviewChoice> = emptyList(),
        /** «Умения в слотах»: переносятся ли навыки; есть смысл, только если они есть в [layout]. */
        val includeSkills: Boolean = true,
        /** «Заметки под слотами»: переносятся ли надписи навыков; без навыков — нет. */
        val includeMemos: Boolean = true,
    ) : SlotsDialog {

        private val choice: PreviewChoice get() = PreviewChoice(included, includedChats)

        /** Отмечено ли хоть что-то: элемент или окно чата. */
        val hasIncluded: Boolean get() = included.isNotEmpty() || includedChats.isNotEmpty()

        /** Новый набор отметок; прежний запоминается для «Отменить», отменённые забываются. */
        fun withIncluded(next: Set<SlotId> = included, nextChats: Set<Int> = includedChats): Preview {
            val nextChoice = PreviewChoice(next, nextChats)
            return if (nextChoice == choice) this else chosen(nextChoice).copy(past = past + choice, future = emptyList())
        }

        fun undone(): Preview {
            val previous = past.lastOrNull() ?: return this
            return chosen(previous).copy(past = past.dropLast(1), future = future + choice)
        }

        fun redone(): Preview {
            val next = future.lastOrNull() ?: return this
            return chosen(next).copy(past = past + choice, future = future.dropLast(1))
        }

        private fun chosen(choice: PreviewChoice) = copy(included = choice.slots, includedChats = choice.chats)
    }

    /** Подтверждение записи в файл игры: игра должна быть закрыта. [target] — в какой набор позиций писать. */
    data class ConfirmApply(val target: GamePreset) : SlotsDialog

    data class ConfirmDelete(val name: String) : SlotsDialog

    /** Надпись навыка [skillNo]: [text] — что введено сейчас, [existing] — была ли надпись до правки. */
    data class EditMemo(val skillNo: Int, val text: String, val existing: Boolean) : SlotsDialog

    /**
     * Выбор навыка для слота перезарядки [slot] с поиском [query]: навыки последнего ранга класса [classKey]
     * в стойке [stance]. [hideAbsolute] — у наследия без «Абсолюта», у которого есть вариант «Мощь».
     */
    data class PickSkill(
        val slot: SlotId,
        val classKey: String,
        val stance: SkillStance,
        val hideAbsolute: Boolean = true,
        val query: String = "",
    ) : SlotsDialog {
        /** Стойка для отбора навыков; у класса «Освобождения» стоек нет — `null`, все навыки класса. */
        val filterStance: SkillStance? get() = stance.takeUnless { CharacterClass.fromKey(classKey)?.ascension == true }
    }

    /**
     * Импорт с навыками: чьи навыки заменить. [layout] — что импортируется, уже без невыбранного; [classKey] — класс
     * навыков импорта (`null` — не определить, тогда предлагаются все персонажи); [chosen] — отмеченные персонажи (`CharacterNo`).
     */
    data class ImportCharacter(val layout: NamedLayout, val classKey: String?, val chosen: Set<String> = emptySet()) : SlotsDialog

    /** Ввод размера окна чата [window] руками. */
}
