package com.layzen.bdotools.update

/** Что показывает плашка обновления. */
sealed interface UpdateState {
    /** Обновления нет, его отложили или программа запущена не из пакета: плашки нет. */
    data object Hidden : UpdateState

    /** Есть новая версия [info]. [portable] — копия из ZIP: её обновляют скачиванием нового архива. */
    data class Available(val info: UpdateInfo, val portable: Boolean, val notesOpen: Boolean = false) : UpdateState

    /** Установщик скачивается: [progress] от 0 до 1, `null` — размер неизвестен. */
    data class Downloading(val info: UpdateInfo, val progress: Float?) : UpdateState

    /** Скачать или запустить установку не удалось; можно повторить. */
    data class Failed(val info: UpdateInfo, val portable: Boolean) : UpdateState
}
