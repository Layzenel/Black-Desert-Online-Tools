package com.layzen.bdotools.update.presentation

import com.layzen.bdotools.update.domain.UpdateInfo

/** Обновление настольной программы: что показывают кнопка в навигации и плашка. */
sealed interface UpdateState {
    /** Обновления нет или программа запущена не из пакета. */
    data object Hidden : UpdateState

    /** Есть новая версия [info]. */
    data class Available(val info: UpdateInfo, val notesOpen: Boolean = false) : UpdateState

    /** Архив новой версии скачивается: [progress] от 0 до 1, `null` — размер неизвестен. */
    data class Downloading(val info: UpdateInfo, val progress: Float?) : UpdateState

    /** Скачать, распаковать или запустить замену не удалось; можно повторить. */
    data class Failed(val info: UpdateInfo) : UpdateState
}
