package com.layzen.bdotools.feature.uieditor.domain.preset

import com.layzen.bdotools.feature.uieditor.domain.game.SlotsRepository

/** Сценарии пресетов пользователя («Мои пресеты»): список, сохранение и удаление. */
class PresetsInteractor(private val repository: SlotsRepository) {

    /** См. [SlotsRepository.presets]. */
    suspend fun presets(account: String): List<NamedLayout> = repository.presets(account)

    /** Сохраняет [preset] (см. [SlotsRepository.savePreset]) и возвращает обновлённый список пресетов. */
    suspend fun save(account: String, preset: NamedLayout): List<NamedLayout> {
        repository.savePreset(account, preset)
        return repository.presets(account)
    }

    /** Удаляет пресет [name] (см. [SlotsRepository.deletePreset]) и возвращает обновлённый список пресетов. */
    suspend fun delete(account: String, name: String): List<NamedLayout> {
        repository.deletePreset(account, name)
        return repository.presets(account)
    }
}
