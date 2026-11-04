package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.feature.slots.domain.LayoutCodec
import com.layzen.bdotools.feature.slots.domain.LayoutFormatException
import com.layzen.bdotools.feature.slots.domain.NamedLayout
import com.layzen.bdotools.feature.slots.domain.SlotLayout
import com.layzen.bdotools.feature.slots.domain.parseGameVariable
import com.layzen.bdotools.feature.slots.domain.writeUiData

private const val PRESET_EXTENSION = ".json"
private val UNSAFE_FILE_CHARS = Regex("""[\\/:*?"<>|]""")

/** Сценарии работы с файлами: загрузка настроек игры, запись раскладки и пресеты пользователя. */
class SlotsRepository(private val storage: SlotsStorage) {

    suspend fun accounts(): List<String> = storage.accounts().ifEmpty { throw SlotsException(SlotsError.NoGameVariable) }

    suspend fun load(account: String): LoadedGame {
        val file = storage.readGameVariable(account)
        val game = try {
            parseGameVariable(file.text)
        } catch (e: IllegalArgumentException) {
            throw SlotsException(SlotsError.NoGameVariable, e.message)
        }
        return LoadedGame(account, game, file)
    }

    /** Записывает [layout] в файл игры. Возвращает путь резервной копии. */
    suspend fun apply(loaded: LoadedGame, layout: SlotLayout): String {
        val text = writeUiData(loaded.file.text, loaded.game.changedEntries(layout))
        return storage.writeGameVariable(loaded.account, text, loaded.file.modified)
    }

    /** Пресеты из папки; файлы, которые не удалось прочитать, пропускаются. */
    suspend fun presets(): List<NamedLayout> = storage.presetFiles().mapNotNull { name ->
        try {
            LayoutCodec.decode(storage.readPreset(name))
        } catch (e: LayoutFormatException) {
            null
        }
    }.sortedBy { it.name.lowercase() }

    /** Сохраняет пресет; пресет с таким же названием перезаписывается. */
    suspend fun savePreset(preset: NamedLayout) {
        storage.writePreset(fileName(preset.name), LayoutCodec.encode(preset))
    }

    suspend fun deletePreset(name: String) {
        storage.deletePreset(fileName(name))
    }

    /** Имя файла для экспорта и папки пресетов: название без символов, запрещённых в Windows. */
    fun fileName(name: String): String = name.replace(UNSAFE_FILE_CHARS, "_").trim().ifEmpty { "layout" } + PRESET_EXTENSION
}
