package com.layzen.bdotools.feature.slots.presentation.state

import com.layzen.bdotools.feature.slots.domain.GamePreset
import com.layzen.bdotools.feature.slots.domain.LayoutError
import com.layzen.bdotools.feature.slots.domain.NamedLayout
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SlotId

/** Открытое модальное окно страницы. */
sealed interface SlotsDialog {
    data class SaveAs(val name: String) : SlotsDialog

    /** Импорт: вставленный текст и ошибка последней попытки его прочитать. */
    data class Import(val text: String, val error: LayoutError? = null) : SlotsDialog

    /**
     * Превью перед импортом или экспортом: раскладка [layout] на экране [screen] — его можно поменять, чтобы
     * посмотреть, как она ляжет на другой монитор. Импортируются или экспортируются только элементы [included].
     */
    data class Preview(
        val mode: PreviewMode,
        val layout: NamedLayout,
        val screen: ScreenSpace,
        val included: Set<SlotId>,
    ) : SlotsDialog

    /** Подтверждение записи в файл игры: игра должна быть закрыта. [target] — в какой набор позиций писать. */
    data class ConfirmApply(val target: GamePreset) : SlotsDialog

    data class ConfirmDelete(val name: String) : SlotsDialog
}
