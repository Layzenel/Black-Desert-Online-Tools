package com.layzen.bdotools.feature.portraitconstructor.data

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

// Папки тулзы: Black Desert\BDO Tools\portraitConstructor\<аккаунт>\…
/** Папка `BDO Tools` внутри `Black Desert`. */
internal const val TOOLS = "BDO Tools"
/** Папка этой тулзы внутри `BDO Tools`. */
internal const val TOOL = "portraitConstructor"
/** Папка резервных копий портретов. */
internal const val BACKUP = "FaceTexture_backup"
/** Папка наборов избранного. */
internal const val FAVORITES = "Favorite FaceTexture"
/** Папка чистых портретов и украшений. */
internal const val CURRENT = "Current portraits"
/** Папка копий файла заметок `gameVariable_lobby.xml`. */
internal const val LOBBY_BACKUP = "lobby_backup"

/** Папка тулзы в прошлой версии: `BDO Tools\portraits` с файлами портретов и папками копий и избранного внутри. */
private const val LEGACY_TOOL = "portraits"

/**
 * Переносит данные прошлых версий в `BDO Tools\portraitConstructor\<аккаунт>\…`, см. [PortraitsStorage.adoptLooseFolders].
 * Ничего не перезаписывает: совпавшее имя получает суффикс `_2`, `_3`.
 */
internal fun adoptLegacyPortraitFolders(root: Path, account: String, characterIds: Set<String>) {
    val tool = root / TOOLS / TOOL
    val legacyTool = root / TOOLS / LEGACY_TOOL
    listOf(BACKUP, FAVORITES).forEach { name ->
        // самые старые версии держали копии и избранное в корне Black Desert, следующие — в BDO Tools\portraits
        listOf(root / name, legacyTool / name).forEach { old -> spreadByAccount(old, account) { owner -> tool / owner / name } }
    }
    // чистые портреты и украшения — только персонажей этого аккаунта: чужие дождутся своего аккаунта
    if (legacyTool.isDirectory()) {
        legacyTool.listDirectoryEntries()
            .filter { it.isRegularFile() && it.nameWithoutExtension in characterIds }
            .forEach { moveFresh(it, tool / account / CURRENT) }
        deleteIfEmpty(legacyTool)
    }
    spreadByAccount(root / TOOLS / LOBBY_BACKUP, account) { owner -> tool / owner / LOBBY_BACKUP }
}

/**
 * Раскладывает содержимое [old] по аккаунтам: папка с номером аккаунта — в [target] этого аккаунта, остальное
 * (копии без аккаунта) — в [target] аккаунта [account]. Опустевшую [old] удаляет.
 */
private fun spreadByAccount(old: Path, account: String, target: (owner: String) -> Path) {
    if (!old.isDirectory()) return
    old.listDirectoryEntries().forEach { entry ->
        if (entry.isDirectory() && isAccountFolder(entry.name)) {
            val to = target(entry.name)
            entry.listDirectoryEntries().forEach { moveFresh(it, to) }
            deleteIfEmpty(entry)
        } else {
            moveFresh(entry, target(account))
        }
    }
    deleteIfEmpty(old)
}

private fun moveFresh(entry: Path, dir: Path) {
    dir.createDirectories()
    entry.moveTo(dir / freshFolderName(dir, base = entry.name))
}

private fun deleteIfEmpty(dir: Path) {
    if (dir.isDirectory() && dir.listDirectoryEntries().isEmpty()) dir.deleteIfExists()
}
