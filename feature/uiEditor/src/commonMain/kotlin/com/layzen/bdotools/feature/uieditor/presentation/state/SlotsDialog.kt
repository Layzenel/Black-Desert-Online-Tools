package com.layzen.bdotools.feature.uieditor.presentation.state

import com.layzen.bdotools.feature.uieditor.domain.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.LayoutError
import com.layzen.bdotools.feature.uieditor.domain.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.SlotId

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

    /** Ввод размера окна чата [window] руками. */
}
