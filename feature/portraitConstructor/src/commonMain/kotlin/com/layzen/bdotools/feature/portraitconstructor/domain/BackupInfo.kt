package com.layzen.bdotools.feature.portraitconstructor.domain

/** Папка резервной копии `<name>` в `BDO Tools\portraits\FaceTexture_backup\<аккаунт>` и персонажи, чьи портреты в ней лежат. */
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
