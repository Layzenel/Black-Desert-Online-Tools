package com.layzen.bdotools.feature.portraits.domain

private const val BMP_EXTENSION = ".bmp"

/**
 * Разбирает список копий от моста: строки `<папка>\t<файл1>,<файл2>`.
 * Пустые папки пропускаются, новые копии идут первыми.
 */
fun parseBackupList(text: String): List<BackupInfo> =
    text.lineSequence()
        .filter { '\t' in it }
        .map { line ->
            val ids = line.substringAfter('\t').split(',')
                .filter { it.endsWith(BMP_EXTENSION, ignoreCase = true) }
                .map { it.dropLast(BMP_EXTENSION.length) }
                .toSet()
            BackupInfo(line.substringBefore('\t'), ids)
        }
        .filter { it.characterIds.isNotEmpty() }
        .sortedWith(compareByDescending<BackupInfo> { it.name.take(19) }.thenByDescending { it.name.length }.thenByDescending { it.name })
        .toList()

/** Копии, в которых есть портрет персонажа, от новых к старым. */
fun List<BackupInfo>.containing(characterId: String): List<BackupInfo> = filter { characterId in it.characterIds }
