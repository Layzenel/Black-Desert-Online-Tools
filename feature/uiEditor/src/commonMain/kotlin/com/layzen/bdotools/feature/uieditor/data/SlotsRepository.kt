package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.feature.uieditor.domain.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.LayoutFormatException
import com.layzen.bdotools.feature.uieditor.domain.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.parseGamePresets
import com.layzen.bdotools.feature.uieditor.domain.writeUiData
import com.layzen.bdotools.feature.uieditor.domain.writeVisibility

private const val PRESET_EXTENSION = ".json"
private val UNSAFE_FILE_CHARS = Regex("""[\\/:*?"<>|]""")

/** Сценарии работы с файлами: загрузка настроек игры, запись раскладки и пресеты пользователя. */
class SlotsRepository(private val storage: SlotsStorage) {

    suspend fun accounts(): List<String> = storage.accounts().ifEmpty { throw SlotsException(SlotsError.NoGameVariable) }

    /** Читает файл аккаунта; в редакторе будет набор [edited], если он есть в файле, иначе общий. */
    suspend fun load(account: String, edited: GamePreset = GamePreset.General): LoadedGame {
        val file = storage.readGameVariable(account)
        val presets = try {
            parseGamePresets(file.text)
        } catch (e: IllegalArgumentException) {
            throw SlotsException(SlotsError.NoGameVariable, e.message)
        }
        return LoadedGame(account, presets, file, edited.takeIf { it in presets } ?: GamePreset.General)
    }

    /**
     * Записывает [layout] в набор [target] файла аккаунта. Видимость общего набора игра хранит ещё и у каждого
     * персонажа, поэтому для него поменявшийся `IsShow` записывается и в файлы персонажей; у пресетов 1–3
     * видимость лежит в самих записях. Возвращает путь резервной копии.
     */
    suspend fun apply(loaded: LoadedGame, layout: SlotLayout, target: GamePreset): String {
        val game = loaded.presets.getValue(target)
        val entries = game.changedEntries(layout)
        val text = writeUiData(loaded.file.text, entries, target)
        val backup = storage.writeGameVariable(loaded.account, text, loaded.file.modified)
        if (target != GamePreset.General) return backup
        val visibility = entries
            .filter { it.isShow != game.uiData.getValue(it.index).isShow }
            .associate { it.index to it.isShow }
        if (visibility.isNotEmpty()) applyVisibility(loaded.account, visibility)
        return backup
    }

    private suspend fun applyVisibility(account: String, visibility: Map<Int, Boolean>) {
        storage.characterFiles(account).forEach { path ->
            val text = storage.readCharacterFile(account, path)
            val changed = writeVisibility(text, visibility)
            if (changed != text) storage.writeCharacterFile(account, path, changed)
        }
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
