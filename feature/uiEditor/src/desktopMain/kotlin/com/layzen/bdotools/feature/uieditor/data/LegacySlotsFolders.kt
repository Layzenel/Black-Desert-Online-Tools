package com.layzen.bdotools.feature.uieditor.data

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name

// Папки тулзы: Black Desert\BDO Tools\uiEditor\<аккаунт>\{backups, presets, background}
internal const val TOOLS = "BDO Tools"
internal const val TOOL = "uiEditor"
internal const val BACKUPS = "backups"
internal const val PRESETS = "presets"
internal const val BACKGROUND = "background"

/** Файл картинки в папке `background` аккаунта. */
internal const val BACKGROUND_FILE = "image"

/** Папка тулзы в прошлых версиях: `BDO Tools\slots`. */
private const val LEGACY_TOOL = "slots"

/** Копия файла игры прошлых версий: `gamevariable_<аккаунт>_…` — аккаунт в имени. */
private val backupOwner = Regex("""^gamevariable_(\d+)_""")

/**
 * Переносит данные прошлых версий из `BDO Tools\slots` в `BDO Tools\uiEditor\<аккаунт>\…`, см. [SlotsStorage.adoptLooseFiles].
 * Где аккаунт известен (папка или имя файла с его номером), данные уходят ему, остальное — аккаунту [account].
 * Ничего не перезаписывает: совпавший файл остаётся на старом месте.
 */
internal fun adoptLegacySlotsFolders(root: Path, account: String) {
    val legacy = root / TOOLS / LEGACY_TOOL
    if (!legacy.isDirectory()) return
    val tool = root / TOOLS / TOOL
    entries(legacy / PRESETS).forEach { entry ->
        if (entry.isDirectory() && isAccountFolder(entry.name)) moveContents(entry, tool / entry.name / PRESETS)
        else moveIfFree(entry, tool / account / PRESETS)
    }
    val background = legacy / BACKGROUND
    if (background.isRegularFile()) {
        moveIfFree(background, tool / account / BACKGROUND, BACKGROUND_FILE)
    } else {
        entries(background).filter { isAccountFolder(it.name) }.forEach { moveIfFree(it, tool / it.name / BACKGROUND, BACKGROUND_FILE) }
    }
    entries(legacy / BACKUPS).forEach { entry ->
        val owner = if (entry.isDirectory()) entry.name.takeIf(::isAccountFolder) else backupOwner.find(entry.name)?.groupValues?.get(1)
        if (entry.isDirectory() && owner != null) moveContents(entry, tool / owner / BACKUPS)
        else moveIfFree(entry, tool / (owner ?: account) / BACKUPS)
    }
    listOf(legacy / PRESETS, legacy / BACKGROUND, legacy / BACKUPS, legacy).forEach(::deleteIfEmpty)
}

/** Папка аккаунта — её имя номер аккаунта. */
private fun isAccountFolder(name: String): Boolean = name.isNotEmpty() && name.all(Char::isDigit)

private fun entries(dir: Path): List<Path> = if (dir.isDirectory()) dir.listDirectoryEntries() else emptyList()

private fun moveContents(from: Path, to: Path) {
    entries(from).forEach { moveIfFree(it, to) }
    deleteIfEmpty(from)
}

/** Переносит [entry] в папку [dir] под именем [name]; если такое уже есть, оставляет [entry] на месте. */
private fun moveIfFree(entry: Path, dir: Path, name: String = entry.name) {
    val target = dir / name
    if (target.exists()) return
    dir.createDirectories()
    entry.moveTo(target)
}

private fun deleteIfEmpty(dir: Path) {
    if (dir.isDirectory() && dir.listDirectoryEntries().isEmpty()) dir.deleteIfExists()
}
