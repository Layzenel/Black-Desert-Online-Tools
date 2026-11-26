package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.core.skill.domain.parseKeyBindings
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Читает слоты перезарядок всех персонажей выбранного аккаунта из файлов игры. Какой персонаж сейчас в игре,
 * эти файлы не говорят: его находит [PlayedLayout].
 * Файлы игры смешанной кодировки читаются как Latin-1: нужны только числа.
 */
internal object SlotLayoutFiles {
    private const val CHARACTERS_DIR = "11"
    private const val USER_CACHE = "UserCache"

    /**
     * Папка аккаунта [account] в папке игры [folder]. Если сохранённого аккаунта там нет (его не выбирали или он
     * устарел), берётся аккаунт с самым свежим `gamevariable.xml` — в нём сейчас и играют.
     */
    fun accountDir(folder: Path, account: String?): Path? {
        val cache = folder.resolve(USER_CACHE).takeIf { it.isDirectory() } ?: return null
        account?.let { cache.resolve(it) }?.takeIf { gameFile(it) != null }?.let { return it }
        return try {
            cache.listDirectoryEntries().filter { it.isDirectory() }
                .mapNotNull { dir -> gameFile(dir)?.let { dir to it.getLastModifiedTime() } }
                .maxByOrNull { it.second }?.first
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Слоты всех персонажей аккаунта [accountDir] по `CharacterNo`; у персонажа, чьи слоты все скрыты или пусты,
     * раскладка без слотов.
     */
    fun readAll(accountDir: Path): Map<String, SlotLayout> = characters(accountDir)

    /** Текст файла аккаунта [accountDir] (как Latin-1) или `null`, если его не прочитать. */
    fun accountText(accountDir: Path): String? = try {
        readGameFile(accountDir)
    } catch (e: IOException) {
        null
    }

    /** Персонажи аккаунта: `CharacterNo` → слоты. Пусто, если файлы не прочитать. */
    private fun characters(accountDir: Path): Map<String, SlotLayout> = try {
        val accountText = readGameFile(accountDir)
        val height = accountText?.let(::parseResolutionHeight)
        if (accountText == null || height == null) {
            emptyMap()
        } else {
            val centers = parseSlotCenters(accountText)
            val hotbarCenters = parseHotbarCenters(accountText)
            val scale = parseUiScale(accountText) ?: 1.0
            characterFiles(accountDir).mapValues { (_, text) ->
                val slots = visibleSlots(parseSlotSkills(text), parseHiddenSlots(text), centers)
                val hotbar = visibleSlots(parseHotbarSkills(text), parseHiddenHotbar(text), hotbarCenters)
                SlotLayout(slots, scale, height, parseKeyBindings(text), parseHistoryShown(text, accountText), hotbar)
            }
        }
    } catch (e: IOException) {
        emptyMap()
    }

    /** Слоты с умениями [skills] (номер слота → умение), кроме скрытых [hidden], на местах [centers]. */
    private fun visibleSlots(skills: Map<Int, Int>, hidden: Set<Int>, centers: Map<Int, Pair<Double, Double>>): List<CooldownSlot> =
        skills.filterKeys { it !in hidden }.mapNotNull { (slot, skillNo) -> centers[slot]?.let { (x, y) -> CooldownSlot(skillNo, x, y) } }

    /** Тексты файлов персонажей по `CharacterNo` (имя папки персонажа). */
    private fun characterFiles(accountDir: Path): Map<String, String> {
        val dir = accountDir.resolve(CHARACTERS_DIR).takeIf { it.isDirectory() } ?: return emptyMap()
        return dir.listDirectoryEntries().filter { it.isDirectory() }.mapNotNull { character ->
            val file = gameFile(character) ?: return@mapNotNull null
            character.name to Files.readString(file, StandardCharsets.ISO_8859_1)
        }.toMap()
    }

    private fun readGameFile(dir: Path): String? = gameFile(dir)?.let { Files.readString(it, StandardCharsets.ISO_8859_1) }

    /** `gamevariable.xml` в папке [dir]: регистр имени у игры бывает разный. */
    private fun gameFile(dir: Path): Path? =
        dir.takeIf { it.isDirectory() }?.listDirectoryEntries()?.firstOrNull { it.name.equals("gamevariable.xml", ignoreCase = true) }
}
