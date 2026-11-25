package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboRepository
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException

/**
 * Комбо и настройки оверлея в одном файле JSON. Испорченный файл читается как пустой список и не перезаписывается, пока игрок
 * не сохранит комбо.
 *
 * @property storage где лежит файл
 */
class ComboRepositoryImpl(private val storage: ComboStorage) : ComboRepository {
    private val lock = Mutex()

    override suspend fun combos(): List<Combo> = lock.withLock { load() }

    override suspend fun save(combo: Combo) = lock.withLock {
        val all = load()
        val updated = if (all.any { it.id == combo.id }) all.map { if (it.id == combo.id) combo else it } else all + combo
        storage.write(combosJson(updated, loadSettings()))
        Unit
    }

    override suspend fun reorder(ids: List<String>) = lock.withLock {
        storage.write(combosJson(load().sortedBy { combo -> ids.indexOf(combo.id).takeIf { it >= 0 } ?: Int.MAX_VALUE }, loadSettings()))
        Unit
    }

    override suspend fun delete(id: String) = lock.withLock {
        storage.write(combosJson(load().filter { it.id != id }, loadSettings()))
        Unit
    }

    override suspend fun settings(): OverlaySettings = lock.withLock { loadSettings() }

    override suspend fun saveSettings(settings: OverlaySettings) = lock.withLock {
        storage.write(combosJson(load(), settings))
        Unit
    }

    private suspend fun loadSettings(): OverlaySettings = try {
        storage.read()?.let(::parseSettings) ?: OverlaySettings()
    } catch (e: SerializationException) {
        OverlaySettings()
    } catch (e: IllegalArgumentException) {
        OverlaySettings()
    }

    private suspend fun load(): List<Combo> = try {
        storage.read()?.let(::parseCombos).orEmpty()
    } catch (e: SerializationException) {
        emptyList()
    } catch (e: IllegalArgumentException) {
        emptyList()
    }
}
