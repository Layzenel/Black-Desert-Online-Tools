package com.layzen.bdotools.feature.settings.data

import com.layzen.bdotools.feature.settings.domain.startup.StartupOptions
import com.layzen.bdotools.feature.settings.domain.startup.StartupRepository

/** Настройки запуска поверх записей системы [startup]. */
class StartupRepositoryImpl(private val startup: SystemStartup) : StartupRepository {
    override suspend fun options(): StartupOptions =
        if (startup.supported) {
            StartupOptions(
                supported = true,
                launchAtStartup = startup.launchesAtStartup(),
                alwaysAdmin = startup.runsAsAdmin(),
                startInTray = startup.startsInTray(),
            )
        } else {
            StartupOptions()
        }

    override suspend fun apply(options: StartupOptions) {
        if (!startup.supported) return
        val current = options()
        if (options.startInTray != current.startInTray) startup.setStartsInTray(options.startInTray)
        // запись в системе (и окно UAC) только при смене её настроек, а не при каждом переключателе
        if (options.alwaysAdmin == current.alwaysAdmin && options.launchAtStartup == current.launchAtStartup) return
        startup.setRunsAsAdmin(options.alwaysAdmin)
        // автозапуск с правами администратора — другим способом, поэтому он ставится заново и при смене прав
        startup.setLaunchAtStartup(options.launchAtStartup, elevated = options.alwaysAdmin)
    }
}
