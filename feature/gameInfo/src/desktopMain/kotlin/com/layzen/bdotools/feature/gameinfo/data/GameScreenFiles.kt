package com.layzen.bdotools.feature.gameinfo.data

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

private val RESOLUTION_HEIGHT = Regex("""<Resolution\s+Width="\d+"\s+Height="(\d+)"""")
private val UI_SCALE = Regex("""<UiScale\s+Value="([\d.]+)"""")

/**
 * Высота экрана игры в UI-px по тексту файла аккаунта: высота разрешения, делённая на «Размер интерфейса»
 * (2560×1440 при 0,8 — 1800); `null`, если разрешения нет. Нет масштаба — интерфейс 100 %.
 */
internal fun parseUiHeight(accountText: String): Double? {
    val height = RESOLUTION_HEIGHT.find(accountText)?.groupValues?.get(1)?.toDoubleOrNull() ?: return null
    val scale = UI_SCALE.find(accountText)?.groupValues?.get(1)?.toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0
    return height / scale
}

/**
 * `gamevariable.xml` аккаунта [account] в папке игры [folder] (прочитанный как Latin-1: нужны только числа). Нет
 * выбранного аккаунта или его файла — берётся самый свежий файл любого аккаунта: в нём сейчас и играют.
 */
internal fun accountText(folder: Path, account: String?): String? {
    val cache = folder.resolve("UserCache").takeIf { it.isDirectory() } ?: return null
    val files = cache.listDirectoryEntries().filter { it.isDirectory() }.mapNotNull { dir ->
        dir.listDirectoryEntries().firstOrNull { it.name.equals("gamevariable.xml", ignoreCase = true) }?.let { dir.name to it }
    }
    val file = files.firstOrNull { it.first == account }?.second ?: files.maxByOrNull { it.second.getLastModifiedTime() }?.second
    return file?.let { Files.readString(it, StandardCharsets.ISO_8859_1) }
}
