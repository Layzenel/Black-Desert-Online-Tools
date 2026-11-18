package com.layzen.bdotools.feature.comboassist.data.slots

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
 * файлы не говорят: его узнаёт [PlayedCharacterDetector] по слотам на экране.
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

    /** Слоты всех персонажей аккаунта [accountDir], у кого они есть. */
    fun readAll(accountDir: Path): List<SlotLayout> = characters(accountDir).map { it.second }.filter { it.slots.isNotEmpty() }

    /** Персонажи аккаунта: время изменения их файла и слоты. Пусто, если файлы не прочитать. */
    private fun characters(accountDir: Path): List<Pair<Long, SlotLayout>> = try {
        val accountText = readGameFile(accountDir)
        val height = accountText?.let(::parseResolutionHeight)
        if (accountText == null || height == null) {
            emptyList()
        } else {
            val centers = parseSlotCenters(accountText)
            val scale = parseUiScale(accountText) ?: 1.0
            characterFiles(accountDir).map { (modified, text) ->
                val hidden = parseHiddenSlots(text)
                val slots = parseSlotSkills(text).filterKeys { it !in hidden }.mapNotNull { (slot, skillNo) ->
                    centers[slot]?.let { (x, y) -> CooldownSlot(skillNo, x, y) }
                }
                modified to SlotLayout(slots, scale, height)
            }
        }
    } catch (e: IOException) {
        emptyList()
    }

    /** Файлы персонажей: время изменения (для сравнения) и текст. */
    private fun characterFiles(accountDir: Path): List<Pair<Long, String>> {
        val dir = accountDir.resolve(CHARACTERS_DIR).takeIf { it.isDirectory() } ?: return emptyList()
        return dir.listDirectoryEntries().filter { it.isDirectory() }.mapNotNull { character ->
            val file = gameFile(character) ?: return@mapNotNull null
            file.getLastModifiedTime().toMillis() to Files.readString(file, StandardCharsets.ISO_8859_1)
        }
    }

    private fun readGameFile(dir: Path): String? = gameFile(dir)?.let { Files.readString(it, StandardCharsets.ISO_8859_1) }

    /** `gamevariable.xml` в папке [dir]: регистр имени у игры бывает разный. */
    private fun gameFile(dir: Path): Path? =
        dir.takeIf { it.isDirectory() }?.listDirectoryEntries()?.firstOrNull { it.name.equals("gamevariable.xml", ignoreCase = true) }
}
