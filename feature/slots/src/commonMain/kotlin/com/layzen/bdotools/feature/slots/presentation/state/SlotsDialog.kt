package com.layzen.bdotools.feature.slots.presentation.state

import com.layzen.bdotools.feature.slots.domain.LayoutError

/** Открытое модальное окно страницы. */
sealed interface SlotsDialog {
    data class SaveAs(val name: String) : SlotsDialog

    /** Импорт: вставленный текст и ошибка последней попытки его прочитать. */
    data class Import(val text: String, val error: LayoutError? = null) : SlotsDialog

    /** Подтверждение записи в файл игры: игра должна быть закрыта. */
    data object ConfirmApply : SlotsDialog

    data class ConfirmDelete(val name: String) : SlotsDialog
}
