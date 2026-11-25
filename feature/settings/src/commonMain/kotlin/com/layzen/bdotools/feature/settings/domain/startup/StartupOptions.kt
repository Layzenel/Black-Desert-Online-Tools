package com.layzen.bdotools.feature.settings.domain.startup

/**
 * Как программа запускается в Windows.
 *
 * @property supported настройки доступны: это установленная настольная версия (на сайте и при запуске из Gradle — нет)
 * @property launchAtStartup программа запускается при входе в Windows
 * @property alwaysAdmin программа всегда запускается от имени администратора
 * @property startInTray при запуске программа не показывает окно, а сразу сворачивается в трей
 */
data class StartupOptions(
    val supported: Boolean = false,
    val launchAtStartup: Boolean = false,
    val alwaysAdmin: Boolean = false,
    val startInTray: Boolean = false,
)
