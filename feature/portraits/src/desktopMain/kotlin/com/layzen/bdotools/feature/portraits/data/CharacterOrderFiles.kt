package com.layzen.bdotools.feature.portraits.data

import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

private const val GAME_VARIABLE_FILE = "gamevariable.xml"
private const val ORDER_START = "<CharacterOrderList Version"
private const val ORDER_END = "</CharacterOrderList>"

/**
 * Фрагменты `<CharacterOrderList…` из `gamevariable.xml` в [cache] (`UserCache`) и его подпапках-аккаунтах.
 * Пустой список, если папки нет. Какой фрагмент подходит к портретам, решает репозиторий.
 */
internal fun characterOrderFragments(cache: Path): List<String> {
    if (!cache.isDirectory()) return emptyList()
    val folders = listOf(cache) + cache.listDirectoryEntries().filter { it.isDirectory() }
    return folders.flatMap { folder ->
        folder.listDirectoryEntries()
            .filter { it.isRegularFile() && it.name.equals(GAME_VARIABLE_FILE, ignoreCase = true) }
            .mapNotNull { orderFragment(it.readText()) }
    }
}

private fun orderFragment(text: String): String? {
    val start = text.indexOf(ORDER_START)
    if (start < 0) return null
    val end = text.indexOf(ORDER_END, start)
    return text.substring(start, if (end < 0) text.length else end).takeIf { it.isNotBlank() }
}
