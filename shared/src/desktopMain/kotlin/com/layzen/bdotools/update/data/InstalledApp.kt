package com.layzen.bdotools.update.data

import com.layzen.bdotools.update.domain.AppVersion
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Запущенная программа: [version] передаёт сборка (`-Dbdotools.version`), [executable] — запускатель
 * `BDO Tools.exe`, который jpackage кладёт в свойство `jpackage.app-path`.
 */
class InstalledApp(val version: AppVersion, val executable: Path) {

    /** Папка программы: её целиком заменяет новая версия из архива. */
    val folder: Path get() = executable.parent

    companion object {
        /**
         * Текущая программа или `null`, если она запущена не из собранного пакета (например, `:desktopApp:run`):
         * тогда обновлять нечего.
         */
        fun current(): InstalledApp? {
            val version = System.getProperty("bdotools.version")?.let(AppVersion::parse) ?: return null
            val executable = System.getProperty("jpackage.app-path")?.let { Paths.get(it) } ?: return null
            return InstalledApp(version, executable)
        }
    }
}
