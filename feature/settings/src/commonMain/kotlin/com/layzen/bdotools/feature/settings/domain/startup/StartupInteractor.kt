package com.layzen.bdotools.feature.settings.domain.startup

/** Автозапуск программы и запуск от имени администратора. */
class StartupInteractor(private val repository: StartupRepository) {
    /** См. [StartupRepository.options]. */
    suspend fun options(): StartupOptions = repository.options()

    /** Включает или выключает автозапуск при входе в Windows и возвращает то, что получилось. */
    suspend fun setLaunchAtStartup(enabled: Boolean): StartupOptions = change { it.copy(launchAtStartup = enabled) }

    /** Включает или выключает запуск от имени администратора и возвращает то, что получилось. */
    suspend fun setAlwaysAdmin(enabled: Boolean): StartupOptions = change { it.copy(alwaysAdmin = enabled) }

    private suspend fun change(edit: (StartupOptions) -> StartupOptions): StartupOptions {
        val current = repository.options()
        if (!current.supported) return current
        repository.apply(edit(current))
        return repository.options()
    }
}
