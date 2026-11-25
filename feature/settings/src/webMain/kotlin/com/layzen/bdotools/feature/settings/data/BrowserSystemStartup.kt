package com.layzen.bdotools.feature.settings.data

/** На сайте программа не запускается в системе: настроек запуска нет. */
internal object BrowserSystemStartup : SystemStartup {
    override val supported: Boolean = false

    override suspend fun launchesAtStartup(): Boolean = false

    override suspend fun runsAsAdmin(): Boolean = false

    override suspend fun setRunsAsAdmin(enabled: Boolean) = Unit

    override suspend fun startsInTray(): Boolean = false

    override suspend fun setStartsInTray(enabled: Boolean) = Unit

    override suspend fun setLaunchAtStartup(enabled: Boolean, elevated: Boolean) = Unit
}

internal actual fun createSystemStartup(): SystemStartup = BrowserSystemStartup
