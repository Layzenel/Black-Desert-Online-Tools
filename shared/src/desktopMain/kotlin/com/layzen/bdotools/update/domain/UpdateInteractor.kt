package com.layzen.bdotools.update.domain

/** Сценарии обновления настольной программы: узнать последнюю сборку, решить, новее ли она, и установить её. */
class UpdateInteractor(private val repository: UpdateRepository) {

    /** См. [UpdateRepository.currentVersion]. */
    val version: AppVersion? get() = repository.currentVersion

    /** См. [UpdateRepository.latest]. */
    suspend fun latest(): UpdateInfo? = repository.latest()

    /** Новее ли сборка [info] запущенной программы; у программы без версии обновлять нечего — `false`. */
    fun isNewer(info: UpdateInfo): Boolean = version?.let { info.version > it } ?: false

    /** См. [UpdateRepository.install]. */
    suspend fun install(info: UpdateInfo, onProgress: (Float) -> Unit) = repository.install(info, onProgress)
}
