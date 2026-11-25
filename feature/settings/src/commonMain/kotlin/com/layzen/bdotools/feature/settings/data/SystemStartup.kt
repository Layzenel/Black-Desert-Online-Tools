package com.layzen.bdotools.feature.settings.data

/** Записи системы о запуске программы; на сайте их нет ([supported] = `false`). Методы сами уходят с главного потока. */
interface SystemStartup {
    /** Можно ли менять запуск: это установленная настольная версия с известным путём к exe. */
    val supported: Boolean

    /** Программа есть в автозапуске (обычном или задачей планировщика с правами администратора). */
    suspend fun launchesAtStartup(): Boolean

    /** У exe стоит флаг совместимости «запускать от имени администратора». */
    suspend fun runsAsAdmin(): Boolean

    /** Ставит или снимает флаг «запускать от имени администратора». */
    suspend fun setRunsAsAdmin(enabled: Boolean)

    /**
     * Ставит автозапуск: [elevated] — задачей планировщика с наивысшими правами (запись из `Run` Windows с правами
     * администратора не запускает), иначе — записью в `Run`. `false` в [enabled] убирает оба способа.
     */
    suspend fun setLaunchAtStartup(enabled: Boolean, elevated: Boolean)
}

/** Реализация [SystemStartup] текущей платформы. */
internal expect fun createSystemStartup(): SystemStartup
