package com.layzen.bdotools.update

import java.lang.ProcessBuilder.Redirect
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Ставит скачанный MSI поверх текущей программы. Пока программа запущена, её файлы заняты, поэтому установку
 * делает отдельный скрипт: ждёт, пока процесс программы завершится, тихо ставит MSI (`/passive` — только полоса
 * прогресса, установка для текущего пользователя, без запроса прав) и снова запускает программу.
 */
object UpdateInstaller {

    /** Запускает скрипт установки и закрывает программу. */
    fun installAndExit(msi: Path, app: InstalledApp): Nothing {
        val script = msi.resolveSibling("install-update.cmd")
        // UTF-8 и chcp 65001: в путях бывает кириллица (имя пользователя), а cmd по умолчанию читает OEM-кодировку
        Files.writeString(script, script(ProcessHandle.current().pid(), msi, app.executable))
        // пути зашиты в сам скрипт: cmd /c ломает кавычки, если в командной строке их больше двух
        ProcessBuilder("cmd.exe", "/c", script.toString())
            .redirectOutput(Redirect.DISCARD)
            .redirectError(Redirect.DISCARD)
            .start()
        exitProcess(0)
    }

    private fun script(pid: Long, msi: Path, executable: Path) = """
        @chcp 65001 >nul
        @echo off
        :wait
        tasklist /fi "PID eq $pid" | find "$pid" >nul && (timeout /t 1 /nobreak >nul & goto wait)
        msiexec /i "$msi" /passive /norestart
        start "" "$executable"
        del "$msi"
    """.trimIndent().replace("\n", "\r\n") + "\r\n"
}
