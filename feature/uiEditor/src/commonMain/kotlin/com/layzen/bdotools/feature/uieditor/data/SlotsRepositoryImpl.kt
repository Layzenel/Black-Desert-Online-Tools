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
import com.layzen.bdotools.feature.uieditor.domain.game.writeSplitHp
import com.layzen.bdotools.feature.uieditor.domain.game.writeUiData
import com.layzen.bdotools.feature.uieditor.domain.game.writeVisibility
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import com.layzen.bdotools.feature.uieditor.domain.skill.parseCharacterSkills
import com.layzen.bdotools.feature.uieditor.domain.skill.writeCharacterSkills

private const val PRESET_EXTENSION = ".json"

/**
 * `Index` элемента, которому игра меняет `IsShow` вместе с «Настройкой быстрых слотов» (видим, пока она включена).
 * Что это за элемент, не выяснено; повторяем за игрой, чтобы файл был таким же, как после переключения в ней.
 */
private const val QUICK_SLOTS_COMPANION_INDEX = 160

/** `Index` панели «здоровье + PVP + гнев» слева вверху: игра показывает её при выключенном «Разделении HP». */
private const val COMBINED_HP_INDEX = 138

/**
 * `Index` элемента без позиции, которому игра меняет `IsShow` вместе с «Разделением HP» (видим, пока оно включено).
 * Что это за элемент, не выяснено; повторяем за игрой (сравнение файлов 2026-11-11).
 */
private const val SPLIT_HP_COMPANION_INDEX = 2
private val UNSAFE_FILE_CHARS = Regex("""[\\/:*?"<>|]""")

/** Сценарии работы с файлами: загрузка настроек игры, запись раскладки и пресеты пользователя. */
class SlotsRepositoryImpl(private val storage: SlotsStorage) : SlotsRepository {

    /** Что переписало последнее применение; `null` — в этом сеансе ничего не применяли или уже откатили. */
    private var journal: ApplyJournal? = null

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
        val splitHp = layout.splitHp?.takeIf { game.splitHpChanged(layout) }
        val chats = game.changedChats(layout)
        var text = writeUiData(loaded.file.text, entries, target)
        text = writeChats(text, chats, game.screen, target)
        if (quickSlots != null) text = writeCustomQuickSlots(text, quickSlots)
        if (splitHp != null) text = writeSplitHp(text, splitHp)
        val companions = companionVisibility(quickSlots, splitHp)
        if (target == GamePreset.General) text = writeVisibility(text, companions)
        val backup = storage.writeGameVariable(loaded.account, text, loaded.file.modified)
        journal = ApplyJournal(loaded.account, loaded.file.text)
        if (target != GamePreset.General) return backup
        val visibility = entries
            .filter { it.isShow != game.uiData.getValue(it.index).isShow }
            .associate { it.index to it.isShow } + companions
        val chatVisibility = chats.filter { (window, chat) -> chat.visible != game.chats[window]?.visible }.mapValues { it.value.visible }
        if (visibility.isNotEmpty() || chatVisibility.isNotEmpty()) {
            applyVisibility(loaded.account, visibility, chatWindowOrder(loaded.file.text), chatVisibility)
        }
        return backup
    }

    /** `IsShow` элементов вне редактора, которые игра меняет вместе с изменёнными настройками ([quickSlots], [splitHp]). */
    private fun companionVisibility(quickSlots: Boolean?, splitHp: Boolean?): Map<Int, Boolean> =
        listOfNotNull(quickSlots?.let { QUICK_SLOTS_COMPANION_INDEX to it }).toMap() +
            (splitHp?.let { mapOf(COMBINED_HP_INDEX to !it, SPLIT_HP_COMPANION_INDEX to it) } ?: emptyMap())

    /** Видимость в файлах персонажей: элементов — по `Index`, окон чата — по порядку их записей ([chatOrder]). */
    private suspend fun applyVisibility(account: String, visibility: Map<Int, Boolean>, chatOrder: List<Int>, chats: Map<Int, Boolean>) {
        storage.characterFiles(account).forEach { path ->
            val text = storage.readCharacterFile(account, path)
            val changed = writeChatVisibility(writeVisibility(text, visibility), chatOrder, chats)
            if (changed != text) writeCharacter(account, path, text, changed)
        }
    }

    override suspend fun characterSkills(account: String): List<CharacterSkills> =
        storage.characterFiles(account).map { path ->
            parseCharacterSkills(characterNoOf(path), storage.readCharacterFile(account, path))
        }

    override suspend fun writeCharacterSkills(account: String, characterNo: String, slots: Map<Int, Int>, memos: Map<Int, String>) {
        val path = storage.characterFiles(account).firstOrNull { characterNoOf(it) == characterNo } ?: return
        val text = storage.readCharacterFile(account, path)
        val changed = writeCharacterSkills(text, slots, memos)
        if (changed != text) writeCharacter(account, path, text, changed)
    }

    /** Пишет файл персонажа, запомнив прежний текст [old] для отката применения. */
    private suspend fun writeCharacter(account: String, path: String, old: String, text: String) {
        journal?.takeIf { it.account == account }?.rememberCharacter(path, old)
        storage.writeCharacterFile(account, path, text)
    }

    override suspend fun undoLastApply(account: String): Boolean {
        val last = journal?.takeIf { it.account == account } ?: return false
        storage.writeGameVariable(account, last.gameVariable, storage.readGameVariable(account).modified)
        last.characterFiles.forEach { (path, text) -> storage.writeCharacterFile(account, path, text) }
        journal = null
        return true
    }

    /** Путь файла персонажа — `11/<CharacterNo>/gamevariable.xml`: номер персонажа — имя папки. */
    private fun characterNoOf(path: String) = path.split('/')[1]

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
