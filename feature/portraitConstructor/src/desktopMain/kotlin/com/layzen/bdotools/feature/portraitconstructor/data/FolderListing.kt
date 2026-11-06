package com.layzen.bdotools.feature.portraitconstructor.data

import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes

// Работа с папками на диске в тех же форматах, что и мост File System Access API в FileSystemBridgeScript.kt

/** Расширение портретов в `FaceTexture`, копиях и избранном. */
internal const val PORTRAIT_EXTENSION = ".bmp"

private val folderStamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

internal fun String.isBmpName(): Boolean = endsWith(PORTRAIT_EXTENSION, ignoreCase = true)

/** Имена файлов в [dir]; пустой список, если папки нет. */
internal fun fileNames(dir: Path): List<String> =
    if (dir.isDirectory()) dir.listDirectoryEntries().filter { it.isRegularFile() }.map { it.name } else emptyList()

/** Папки внутри [dir] строками `<папка>\t<портрет1>.bmp,<портрет2>.bmp`; пустая строка, если папки нет. */
internal fun folderListing(dir: Path): String {
    if (!dir.isDirectory()) return ""
    return dir.listDirectoryEntries()
        .filter { it.isDirectory() }
        .joinToString("\n") { folder -> folder.name + "\t" + fileNames(folder).filter { it.isBmpName() }.joinToString(",") }
}

/**
 * Имя новой папки (или файла с расширением [extension]) внутри [parent] по текущим дате и времени: `2026-11-04_18-30-05`.
 * Если такое уже есть (две записи в одну секунду), добавляется номер: `…_2`, `…_3`. Расширение в имя не входит.
 */
internal fun freshFolderName(parent: Path, extension: String = ""): String {
    val stamp = LocalDateTime.now().format(folderStamp)
    var folder = stamp
    var number = 2
    while (parent.resolve(folder + extension).exists()) folder = stamp + "_" + number++
    return folder
}

/** Содержимое файла или `null`, если его нет или прочитать не удалось. */
internal fun readOrNull(file: Path): ByteArray? =
    try {
        if (file.isRegularFile()) file.readBytes() else null
    } catch (e: IOException) {
        null
    }

/** Удаляет папку со всем содержимым; если её уже нет — ничего не делает. */
@OptIn(ExperimentalPathApi::class)
internal fun deleteFolder(dir: Path) {
    if (dir.exists()) dir.deleteRecursively()
}
