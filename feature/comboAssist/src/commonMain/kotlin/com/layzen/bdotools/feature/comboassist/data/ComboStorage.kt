package com.layzen.bdotools.feature.comboassist.data

/**
 * Где лежит файл комбо: на сайте — `localStorage` (без доступа к папке игры), в настольной версии —
 * `Документы\Black Desert\BDO Tools\comboAssist\combos.json`. Ошибки чтения и записи не бросаются:
 * без файла редактор работает с пустым списком.
 */
interface ComboStorage {

    /** Текст файла комбо или `null`, если его ещё нет или он не читается. */
    suspend fun read(): String?

    /** Записывает [text]; `false`, если записать не удалось. */
    suspend fun write(text: String): Boolean
}

/** Хранилище комбо своей платформы. */
internal expect fun createComboStorage(): ComboStorage
