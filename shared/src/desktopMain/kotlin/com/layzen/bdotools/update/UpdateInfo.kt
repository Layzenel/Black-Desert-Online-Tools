package com.layzen.bdotools.update

/**
 * Последняя сборка из файла `update.txt` в релизе `desktop-latest`: её версия, ссылки на установщик
 * и портативный архив и список изменений (заголовки влитых PR).
 */
data class UpdateInfo(
    val version: AppVersion,
    val msiUrl: String,
    val zipUrl: String,
    val notes: List<String>,
) {
    companion object {
        /**
         * Разбирает строки `ключ=значение`: `version`, `msi`, `zip` и сколько угодно `note`.
         * Пустые и неизвестные строки пропускаются; без версии или ссылки на MSI — `null`.
         */
        fun parse(text: String): UpdateInfo? {
            val entries = text.lineSequence()
                .map { it.trim() }
                .filter { '=' in it }
                .map { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
                .toList()
            fun value(key: String) = entries.firstOrNull { it.first == key }?.second?.takeIf { it.isNotEmpty() }
            val version = value("version")?.let(AppVersion::parse) ?: return null
            val msi = value("msi") ?: return null
            return UpdateInfo(
                version = version,
                msiUrl = msi,
                zipUrl = value("zip").orEmpty(),
                notes = entries.filter { it.first == "note" && it.second.isNotEmpty() }.map { it.second },
            )
        }
    }
}
