package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboFiles
import kotlinx.serialization.SerializationException

/**
 * Файлы комбо в том же формате, что и хранилище ([combosJson]): экспортированный файл можно и импортировать,
 * и положить вместо `combos.json`. Имя файла — название комбо.
 *
 * @property device окна выбора и сохранения файла своей платформы
 */
class ComboFilesImpl(private val device: DeviceTextFiles) : ComboFiles {
    override suspend fun export(combo: Combo) {
        device.save(fileName(combo), combosJson(listOf(combo)))
    }

    override suspend fun import(): List<Combo>? {
        val text = device.open() ?: return null
        return try {
            parseCombos(text)
        } catch (e: SerializationException) {
            throw IllegalArgumentException("not a combo file", e)
        }
    }

    private fun fileName(combo: Combo): String =
        combo.name.ifBlank { combo.classKey }.replace(UNSAFE, "_") + ".json"

    private companion object {
        /** Знаки, которых не бывает в имени файла Windows. */
        val UNSAFE = Regex("""[\/:*?"<>|]""")
    }
}
