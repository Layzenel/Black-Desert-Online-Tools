package com.layzen.bdotools.update

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

/**
 * Распаковка портативного архива `BDO-Tools-portable.zip`. Внутри архива — папка программы целиком
 * (`BDO Tools.exe`, `app`, `runtime`); её содержимым скрипт обновления заменяет файлы установленной копии.
 */
object UpdateArchive {

    /**
     * Распаковывает [zip] в [target] и возвращает папку, в которой лежит запускатель с именем [executableName].
     * Записи, которые ведут за пределы [target] (`../`), считаются повреждённым архивом.
     */
    fun extract(zip: Path, target: Path, executableName: String): Path {
        val root = target.toAbsolutePath().normalize()
        ZipInputStream(Files.newInputStream(zip)).use { input ->
            while (true) {
                val entry = input.nextEntry ?: break
                // Compress-Archive пишет обратные косые черты
                val path = root.resolve(entry.name.replace('\\', '/')).normalize()
                if (!path.startsWith(root)) throw IOException("Unsafe path in archive: ${entry.name}")
                if (entry.isDirectory) {
                    Files.createDirectories(path)
                } else {
                    Files.createDirectories(path.parent)
                    Files.copy(input, path)
                }
            }
        }
        return appFolder(root, executableName)
    }

    /** Папка с запускателем: сам [root] или одна из его подпапок первого уровня. */
    private fun appFolder(root: Path, executableName: String): Path {
        if (Files.exists(root.resolve(executableName))) return root
        Files.list(root).use { children ->
            return children.filter { Files.exists(it.resolve(executableName)) }.findFirst()
                .orElseThrow { IOException("$executableName not found in archive") }
        }
    }
}
