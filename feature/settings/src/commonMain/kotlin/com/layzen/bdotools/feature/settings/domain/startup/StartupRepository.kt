package com.layzen.bdotools.feature.settings.domain.startup

/** Настройки запуска программы в системе: автозапуск и права администратора. */
interface StartupRepository {
    /** Текущие настройки, прочитанные из системы. */
    suspend fun options(): StartupOptions

    /**
     * Записывает настройки [options] в систему. Автозапуск от администратора требует подтверждения UAC: если его не дали,
     * часть настроек не запишется — что получилось, покажет следующий вызов [options].
     */
    suspend fun apply(options: StartupOptions)
}
