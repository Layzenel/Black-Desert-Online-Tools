package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.core.folder.GameFolderSettings
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private const val CACHE = "UserCache"
private const val GAME_FILE = "gamevariable.xml"
private const val SERVERS = "11"
private const val TOOLS = "BDO Tools"
private const val SLOTS = "slots"
private const val BACKUPS = "backups"
private const val PRESETS = "presets"
private const val BACKGROUND = "background"
private const val PRESET_EXTENSION = ".json"
private const val GAME_FOLDER = "Black Desert"

private val stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

/**
 * Файлы на диске через `java.nio.file` для настольной версии. Папки, имена копий и форматы те же, что у моста
 * в браузере (`SlotsBridgeScript.kt`), поэтому [SlotsRepository] работает одинаково.
 *
 * Папка выбирается системным окном [pickFolder] и запоминается в [settings] — общих для всех тулз.
 * Разрешений, как в браузере, не бывает: если сохранённая папка пропала или в ней нет `UserCache`,
 * доступ — [FolderAccess.NotSelected]. Файлы игры читаются и пишутся в Latin-1, по символу на байт: в них
 * смешаны UTF-8 и Windows-1251 (надписи слотов перезарядки, заметки), а перекодировка через UTF-8 портит
 * кириллицу. Свои файлы тулзы (пресеты) — в UTF-8.
 */
internal class DesktopSlotsStorage(
    private val settings: GameFolderSettings,
    private val pickFolder: suspend () -> Path?,
) : SlotsStorage {

    override suspend fun access(): FolderAccess = fileCall {
        val folder = settings.folder?.takeIf(::hasCache)
        if (folder == null) FolderAccess.NotSelected else FolderAccess.Granted(folder.name)
    }

    override suspend fun selectFolder(): FolderAccess.Granted {
        val picked = pickFolder() ?: throw SlotsException(SlotsError.Cancelled)
        return fileCall {
            val root = gameFolder(picked) ?: throw SlotsException(SlotsError.NotGameFolder)
            settings.folder = root
            FolderAccess.Granted(root.name)
        }
    }

    override suspend fun requestPermission(): FolderAccess.Granted =
        access() as? FolderAccess.Granted ?: throw SlotsException(SlotsError.NotGameFolder)

    /** Аккаунты по свежести файла: игра пишет в папку того аккаунта, под которым играли последним. */
    override suspend fun accounts(): List<String> = fileCall {
        val cache = root() / CACHE
        if (!cache.isDirectory()) return@fileCall emptyList()
        cache.listDirectoryEntries()
            .filter { it.isDirectory() }
            .mapNotNull { dir -> gameFile(dir)?.let { dir.name to it.getLastModifiedTime().toMillis() } }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    override suspend fun readGameVariable(account: String): GameFile = fileCall {
        val file = accountFile(account)
        GameFile(text = file.readBytes().toString(Charsets.ISO_8859_1), modified = file.getLastModifiedTime().toMillis())
    }

    override suspend fun writeGameVariable(account: String, text: String, expectedModified: Long): String = fileCall {
        val file = accountFile(account)
        if (file.getLastModifiedTime().toMillis() != expectedModified) throw SlotsException(SlotsError.FileChanged)
        val backupName = "gamevariable_${account}_${now()}.xml"
        file.copyTo(backups(account) / backupName)
        file.writeBytes(text.toByteArray(Charsets.ISO_8859_1))
        "$TOOLS\\$SLOTS\\$BACKUPS\\$account\\$backupName"
    }

    override suspend fun characterFiles(account: String): List<String> = fileCall {
        val servers = root() / CACHE / account / SERVERS
        if (!servers.isDirectory()) return@fileCall emptyList()
        servers.listDirectoryEntries()
            .filter { it.isDirectory() }
            .mapNotNull { dir -> gameFile(dir)?.let { "$SERVERS/${dir.name}/${it.name}" } }
    }

    override suspend fun readCharacterFile(account: String, path: String): String = fileCall {
        characterFile(account, path).readBytes().toString(Charsets.ISO_8859_1)
    }

    override suspend fun writeCharacterFile(account: String, path: String, text: String) = fileCall {
        val file = characterFile(account, path)
        file.copyTo(backups(account) / "gamevariable_${account}_${path.split('/')[1]}_${now()}.xml")
        file.writeBytes(text.toByteArray(Charsets.ISO_8859_1))
    }

    override suspend fun presetFiles(): List<String> = fileCall {
        val dir = tools() / PRESETS
        if (!dir.isDirectory()) return@fileCall emptyList()
        dir.listDirectoryEntries().filter { it.isRegularFile() && it.name.endsWith(PRESET_EXTENSION, ignoreCase = true) }.map { it.name }
    }

    override suspend fun readPreset(fileName: String): String = fileCall { (tools() / PRESETS / fileName).readBytes().decodeToString() }

    override suspend fun writePreset(fileName: String, text: String) = fileCall {
        ((tools() / PRESETS).createDirectories() / fileName).writeBytes(text.encodeToByteArray())
    }

    override suspend fun deletePreset(fileName: String) = fileCall {
        (tools() / PRESETS / fileName).deleteIfExists()
        Unit
    }

    override suspend fun readBackground(): ByteArray? = fileCall {
        (tools() / BACKGROUND).takeIf { it.isRegularFile() }?.readBytes()
    }

    override suspend fun writeBackground(bytes: ByteArray?) = fileCall {
        val file = tools() / BACKGROUND
        if (bytes == null) {
            file.deleteIfExists()
        } else {
            tools().createDirectories()
            file.writeBytes(bytes)
        }
        Unit
    }

    private fun root(): Path = settings.folder?.takeIf(::hasCache) ?: throw SlotsException(SlotsError.NotGameFolder)

    private fun tools(): Path = root() / TOOLS / SLOTS

    /**
     * `BDO Tools\slots\backups\<account>` — копии файлов игры аккаунта. Копии из версий до аккаунтов лежат прямо
     * в `backups` с аккаунтом в имени (`gamevariable_<account>_…`) и переезжают сюда при следующей записи.
     */
    private fun backups(account: String): Path {
        val all = (tools() / BACKUPS).createDirectories()
        val dir = (all / account).createDirectories()
        all.listDirectoryEntries("gamevariable_${account}_*").filter { it.isRegularFile() }.forEach { it.moveTo(dir / it.name) }
        return dir
    }

    private fun accountFile(account: String): Path =
        gameFile(root() / CACHE / account) ?: throw SlotsException(SlotsError.NoGameVariable)

    /** [path] — «11/<персонаж>/<имя файла>» от папки аккаунта, как в списке [characterFiles]. */
    private fun characterFile(account: String, path: String): Path {
        val (server, id, name) = path.split('/')
        return root() / CACHE / account / server / id / name
    }

    /** `gamevariable.xml` в [dir]; у персонажей имя бывает и `gameVariable.xml`. */
    private fun gameFile(dir: Path): Path? =
        if (dir.isDirectory()) dir.listDirectoryEntries().firstOrNull { it.isRegularFile() && it.name.equals(GAME_FILE, ignoreCase = true) } else null

    private fun hasCache(dir: Path): Boolean = (dir / CACHE).isDirectory()

    /** Подходит `Black Desert` или папка, в которой она лежит, — как в браузере. */
    private fun gameFolder(picked: Path): Path? = when {
        hasCache(picked) -> picked
        hasCache(picked / GAME_FOLDER) -> picked / GAME_FOLDER
        else -> null
    }

    private fun now(): String = LocalDateTime.now().format(stamp)
}
