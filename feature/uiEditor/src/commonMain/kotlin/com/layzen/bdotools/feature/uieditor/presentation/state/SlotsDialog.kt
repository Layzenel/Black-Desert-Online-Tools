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
     * Импортируются или экспортируются только элементы [included]. [past] и [future] — прежние и отменённые
     * наборы отметок для «Отменить» и «Повторить».
     */
    data class Preview(
        val mode: PreviewMode,
        val layout: NamedLayout,
        val screen: ScreenSpace,
        val included: Set<SlotId>,
        val past: List<Set<SlotId>> = emptyList(),
        val future: List<Set<SlotId>> = emptyList(),
    ) : SlotsDialog {

        /** Новый набор отметок; прежний запоминается для «Отменить», отменённые забываются. */
        fun withIncluded(next: Set<SlotId>): Preview =
            if (next == included) this else copy(included = next, past = past + listOf(included), future = emptyList())

        fun undone(): Preview {
            val previous = past.lastOrNull() ?: return this
            return copy(included = previous, past = past.dropLast(1), future = future + listOf(included))
        }

        fun redone(): Preview {
            val next = future.lastOrNull() ?: return this
            return copy(included = next, past = past + listOf(included), future = future.dropLast(1))
        }
    }

    /** Подтверждение записи в файл игры: игра должна быть закрыта. [target] — в какой набор позиций писать. */
    data class ConfirmApply(val target: GamePreset) : SlotsDialog

    data class ConfirmDelete(val name: String) : SlotsDialog
}
