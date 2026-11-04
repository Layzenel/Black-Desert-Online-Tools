package com.layzen.bdotools.update

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** Файл-метка рядом с `BDO Tools.exe` в портативном архиве: такую копию обновляют скачиванием нового ZIP. */
private const val PORTABLE_MARKER = "portable.txt"

/**
 * Запущенная программа: [version] передаёт сборка (`-Dbdotools.version`), [executable] — запускатель
 * `BDO Tools.exe`, который jpackage кладёт в свойство `jpackage.app-path`.
 */
class InstalledApp(val version: AppVersion, val executable: Path, val isPortable: Boolean) {

    companion object {
        /**
         * Текущая программа или `null`, если она запущена не из собранного пакета (например, `:desktopApp:run`):
         * тогда обновлять нечего.
         */
        fun current(): InstalledApp? {
            val version = System.getProperty("bdotools.version")?.let(AppVersion::parse) ?: return null
            val executable = System.getProperty("jpackage.app-path")?.let { Paths.get(it) } ?: return null
            val portable = executable.parent?.let { Files.exists(it.resolve(PORTABLE_MARKER)) } == true
            return InstalledApp(version, executable, portable)
        }
    }
}
