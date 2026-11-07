package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.feature.uieditor.domain.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.LayoutFormatException
import com.layzen.bdotools.feature.uieditor.domain.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.parseGamePresets
import com.layzen.bdotools.feature.uieditor.domain.writeUiData
import com.layzen.bdotools.feature.uieditor.domain.writeCustomQuickSlots
import com.layzen.bdotools.feature.uieditor.domain.writeVisibility
import com.layzen.bdotools.feature.uieditor.domain.chatWindowOrder
import com.layzen.bdotools.feature.uieditor.domain.writeChatVisibility
import com.layzen.bdotools.feature.uieditor.domain.writeChats

private const val PRESET_EXTENSION = ".json"

/**
 * `Index` элемента, которому игра меняет `IsShow` вместе с «Настройкой быстрых слотов» (видим, пока она включена).
 * Что это за элемент, не выяснено; повторяем за игрой, чтобы файл был таким же, как после переключения в ней.
 */
private const val QUICK_SLOTS_COMPANION_INDEX = 160
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
     * видимость лежит в самих записях. «Настройка быстрых слотов» одна на аккаунт и пишется при любом [target],
     * если поменялась. Окна чата пишутся в тот же набор, их видимость в общем наборе — ещё и персонажам.
     * Возвращает путь резервной копии.
     */
    suspend fun apply(loaded: LoadedGame, layout: SlotLayout, target: GamePreset): String {
        val game = loaded.presets.getValue(target)
        val entries = game.changedEntries(layout)
        val quickSlots = layout.customQuickSlots?.takeIf { game.quickSlotsChanged(layout) }
        val chats = game.changedChats(layout)
        var text = writeUiData(loaded.file.text, entries, target)
        text = writeChats(text, chats, game.screen, target)
        if (quickSlots != null) text = writeCustomQuickSlots(text, quickSlots)
        if (quickSlots != null && target == GamePreset.General) {
            text = writeVisibility(text, mapOf(QUICK_SLOTS_COMPANION_INDEX to quickSlots))
        }
        val backup = storage.writeGameVariable(loaded.account, text, loaded.file.modified)
        if (target != GamePreset.General) return backup
        val visibility = entries
            .filter { it.isShow != game.uiData.getValue(it.index).isShow }
            .associate { it.index to it.isShow } +
            listOfNotNull(quickSlots?.let { QUICK_SLOTS_COMPANION_INDEX to it })
        val chatVisibility = chats.filter { (window, chat) -> chat.visible != game.chats[window]?.visible }.mapValues { it.value.visible }
        if (visibility.isNotEmpty() || chatVisibility.isNotEmpty()) {
            applyVisibility(loaded.account, visibility, chatWindowOrder(loaded.file.text), chatVisibility)
        }
        return backup
    }

    /** Видимость в файлах персонажей: элементов — по `Index`, окон чата — по порядку их записей ([chatOrder]). */
    private suspend fun applyVisibility(account: String, visibility: Map<Int, Boolean>, chatOrder: List<Int>, chats: Map<Int, Boolean>) {
        storage.characterFiles(account).forEach { path ->
            val text = storage.readCharacterFile(account, path)
            val changed = writeChatVisibility(writeVisibility(text, visibility), chatOrder, chats)
            if (changed != text) storage.writeCharacterFile(account, path, changed)
        }
    }

    /** Пресеты аккаунта [account]; файлы, которые не удалось прочитать, пропускаются. */
    suspend fun presets(account: String): List<NamedLayout> = storage.presetFiles(account).mapNotNull { name ->
        try {
            LayoutCodec.decode(storage.readPreset(account, name))
        } catch (e: LayoutFormatException) {
            null
        }
    }.sortedBy { it.name.lowercase() }

    /** Сохраняет пресет аккаунта [account]; пресет с таким же названием перезаписывается. */
    suspend fun savePreset(account: String, preset: NamedLayout) {
        storage.writePreset(account, fileName(preset.name), LayoutCodec.encode(preset))
    }

    suspend fun deletePreset(account: String, name: String) {
        storage.deletePreset(account, fileName(name))
    }

    /** Имя файла для экспорта и папки пресетов: название без символов, запрещённых в Windows. */
    fun fileName(name: String): String = name.replace(UNSAFE_FILE_CHARS, "_").trim().ifEmpty { "layout" } + PRESET_EXTENSION
}
