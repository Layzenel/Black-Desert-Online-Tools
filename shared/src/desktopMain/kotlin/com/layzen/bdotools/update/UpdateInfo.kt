package com.layzen.bdotools.update

/**
 * Последняя сборка из файла `update.txt` в релизе `desktop-latest`: её версия, ссылка на портативный архив,
 * которым программа обновляет себя, и список изменений (заголовки влитых PR).
 */
data class UpdateInfo(
    val version: AppVersion,
    val zipUrl: String,
    val notes: List<String>,
) {
    companion object {
        /**
         * Разбирает строки `ключ=значение`: `version`, `zip` и сколько угодно `note`. Строку `msi` читают
         * только старые версии программы, которые обновлялись установщиком.
         * Пустые и неизвестные строки пропускаются; без версии или ссылки на архив — `null`.
         */
        fun parse(text: String): UpdateInfo? {
            val entries = text.lineSequence()
                .map { it.trim() }
                .filter { '=' in it }
                .map { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
                .toList()
            fun value(key: String) = entries.firstOrNull { it.first == key }?.second?.takeIf { it.isNotEmpty() }
            val version = value("version")?.let(AppVersion::parse) ?: return null
            val zip = value("zip") ?: return null
            return UpdateInfo(
                version = version,
                zipUrl = zip,
                notes = entries.filter { it.first == "note" && it.second.isNotEmpty() }.map { it.second },
            )
        }
    }
}
