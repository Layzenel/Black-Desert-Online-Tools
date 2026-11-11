package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsError
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsException
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutFormatException
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.game.LoadedGame
import com.layzen.bdotools.feature.uieditor.domain.game.SlotsRepository
import com.layzen.bdotools.feature.uieditor.domain.game.chatWindowOrder
import com.layzen.bdotools.feature.uieditor.domain.game.parseGamePresets
import com.layzen.bdotools.feature.uieditor.domain.game.writeChatVisibility
import com.layzen.bdotools.feature.uieditor.domain.game.writeChats
import com.layzen.bdotools.feature.uieditor.domain.game.writeCustomQuickSlots
import com.layzen.bdotools.feature.uieditor.domain.game.writeUiData
import com.layzen.bdotools.feature.uieditor.domain.game.writeVisibility
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout

private const val PRESET_EXTENSION = ".json"

/**
 * `Index` элемента, которому игра меняет `IsShow` вместе с «Настройкой быстрых слотов» (видим, пока она включена).
 * Что это за элемент, не выяснено; повторяем за игрой, чтобы файл был таким же, как после переключения в ней.
 */
private const val QUICK_SLOTS_COMPANION_INDEX = 160
private val UNSAFE_FILE_CHARS = Regex("""[\\/:*?"<>|]""")

/** Сценарии работы с файлами: загрузка настроек игры, запись раскладки и пресеты пользователя. */
class SlotsRepositoryImpl(private val storage: SlotsStorage) : SlotsRepository {

    override suspend fun accounts(): List<String> = storage.accounts().ifEmpty { throw SlotsException(SlotsError.NoGameVariable) }

    override suspend fun load(account: String, edited: GamePreset): LoadedGame {
        val file = storage.readGameVariable(account)
        val presets = try {
            parseGamePresets(file.text)
        } catch (e: IllegalArgumentException) {
            throw SlotsException(SlotsError.NoGameVariable, e.message)
        }
        return LoadedGame(account, presets, file, edited.takeIf { it in presets } ?: GamePreset.General)
    }

    override suspend fun apply(loaded: LoadedGame, layout: SlotLayout, target: GamePreset): String {
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

    override suspend fun presets(account: String): List<NamedLayout> = storage.presetFiles(account).mapNotNull { name ->
        try {
            LayoutCodec.decode(storage.readPreset(account, name))
        } catch (e: LayoutFormatException) {
            null
        }
    }.sortedBy { it.name.lowercase() }

    override suspend fun savePreset(account: String, preset: NamedLayout) {
        storage.writePreset(account, fileName(preset.name), LayoutCodec.encode(preset))
    }

    override suspend fun deletePreset(account: String, name: String) {
        storage.deletePreset(account, fileName(name))
    }

    override fun fileName(name: String): String = name.replace(UNSAFE_FILE_CHARS, "_").trim().ifEmpty { "layout" } + PRESET_EXTENSION

    override suspend fun access(): FolderAccess = storage.access()

    override suspend fun selectFolder(): FolderAccess.Granted = storage.selectFolder()

    override suspend fun requestPermission(): FolderAccess.Granted = storage.requestPermission()

    override suspend fun adoptLooseFiles(account: String) = storage.adoptLooseFiles(account)

    override suspend fun readBackground(account: String): ByteArray? = storage.readBackground(account)

    override suspend fun writeBackground(account: String, bytes: ByteArray?) = storage.writeBackground(account, bytes)
}
