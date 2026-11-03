package com.layzen.bdotools.feature.portraits.domain

private const val BMP_EXTENSION = ".bmp"

/** Папка `FaceTexture_backup\<name>` и персонажи, чьи портреты в ней лежат. */
data class BackupInfo(val name: String, val characterIds: Set<String>) {
    /** `2026-11-03_12-00-05` → `2026-11-03 12:00:05`. Повторная копия в ту же секунду получает суффикс `_2`. */
    val label: String
        get() {
            val match = BACKUP_NAME.matchEntire(name) ?: return name
            val (date, hours, minutes, seconds, suffix) = match.destructured
            return "$date $hours:$minutes:$seconds" + if (suffix.isEmpty()) "" else " ($suffix)"
        }

    private companion object {
        val BACKUP_NAME = Regex("""(\d{4}-\d{2}-\d{2})_(\d{2})-(\d{2})-(\d{2})(?:_(\d+))?""")
    }
}

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
