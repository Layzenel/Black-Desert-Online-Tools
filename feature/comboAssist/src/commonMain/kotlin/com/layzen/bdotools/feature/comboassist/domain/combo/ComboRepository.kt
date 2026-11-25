package com.layzen.bdotools.feature.comboassist.domain.combo

import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings

/**
 * Сохранённые комбо игрока. Комбо привязаны к классу, а не к аккаунту и персонажу: одно комбо подходит всем
 * персонажам класса на любом аккаунте.
 */
interface ComboRepository {

    /** Все комбо по порядку сохранения; пусто, если их ещё нет или хранилище не читается. */
    suspend fun combos(): List<Combo>

    /** Сохраняет [combo]: заменяет комбо с тем же [Combo.id] или добавляет новое. */
    suspend fun save(combo: Combo)

    /** Меняет порядок комбо на порядок [ids]; комбо, которых нет в [ids], остаются в конце. */
    suspend fun reorder(ids: List<String>)

    /** Удаляет комбо [id]. */
    suspend fun delete(id: String)

    /** Настройки оверлея; по умолчанию, если их ещё не сохраняли. */
    suspend fun settings(): OverlaySettings

    /** Сохраняет настройки оверлея [settings]. */
    suspend fun saveSettings(settings: OverlaySettings)
}
