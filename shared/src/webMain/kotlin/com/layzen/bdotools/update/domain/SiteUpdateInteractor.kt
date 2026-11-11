package com.layzen.bdotools.update.domain

/** Сценарий обновления сайта: узнать, выложена ли версия, отличная от открытой. */
internal class SiteUpdateInteractor(private val repository: SiteVersionRepository) {

    /** См. [SiteVersionRepository.ownVersion]. */
    val ownVersion: String? get() = repository.ownVersion

    /**
     * Выложенная версия, если она отличается от открытой; иначе `null`. У сборки разработчика версии нет,
     * и для неё ответ всегда `null`.
     */
    suspend fun newVersion(): String? {
        val own = repository.ownVersion ?: return null
        return repository.deployedVersion()?.takeIf { it != own }
    }
}
