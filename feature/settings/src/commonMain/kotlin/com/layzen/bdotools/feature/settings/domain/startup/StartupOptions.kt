package com.layzen.bdotools.feature.settings.domain.startup

/**
 * Как программа запускается в Windows.
 *
 * @property supported настройки доступны: это установленная настольная версия (на сайте и при запуске из Gradle — нет)
 * @property launchAtStartup программа запускается при входе в Windows
 * @property alwaysAdmin программа всегда запускается от имени администратора
 */
data class StartupOptions(
    val supported: Boolean = false,
    val launchAtStartup: Boolean = false,
    val alwaysAdmin: Boolean = false,
)
