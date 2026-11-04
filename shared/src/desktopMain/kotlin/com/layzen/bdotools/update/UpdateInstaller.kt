package com.layzen.bdotools.update

import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.update_script_copying
import com.layzen.bdotools.resources.update_script_failed
import com.layzen.bdotools.resources.update_script_title
import com.layzen.bdotools.resources.update_script_waiting
import java.lang.ProcessBuilder.Redirect
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import org.jetbrains.compose.resources.getString

private const val SCRIPT_NAME = "install-update.cmd"

/**
 * Заменяет файлы программы новой версией из распакованного архива. Пока программа запущена, её файлы заняты,
 * поэтому замену делает отдельный скрипт в окне консоли: ждёт, пока процесс программы завершится,
 * копирует новую папку поверх старой (`robocopy /MIR` — лишние старые файлы удаляются) и снова запускает
 * программу. Если скопировать не удалось, запускает то, что осталось в папке. Так обновляются и портативная
 * копия, и установленная через MSI: установка идёт для текущего пользователя, папка доступна на запись.
 */
object UpdateInstaller {

    /**
     * Запускает скрипт замены и закрывает программу. [newApp] — распакованная папка новой версии,
     * [workDir] — временная папка обновления: скрипт удалит её в конце.
     */
    suspend fun installAndExit(newApp: Path, workDir: Path, app: InstalledApp): Nothing {
        val texts = ScriptTexts(
            title = getString(Res.string.update_script_title),
            waiting = getString(Res.string.update_script_waiting),
            copying = getString(Res.string.update_script_copying),
            failed = getString(Res.string.update_script_failed),
        )
        // UTF-8 и chcp 65001: в путях бывает кириллица (имя пользователя), а cmd по умолчанию читает OEM-кодировку
        Files.writeString(workDir.resolve(SCRIPT_NAME), script(ProcessHandle.current().pid(), newApp, workDir, app, texts))
        // Java запускает процессы без окна, поэтому окно консоли открывает start. Пути и заголовок зашиты
        // в сам скрипт, а он запускается по короткому имени из своей папки: cmd /c ломает кавычки
        ProcessBuilder("cmd.exe", "/c", "start", SCRIPT_NAME)
            .directory(workDir.toFile())
            .redirectOutput(Redirect.DISCARD)
            .redirectError(Redirect.DISCARD)
            .start()
        exitProcess(0)
    }

    private fun script(pid: Long, newApp: Path, workDir: Path, app: InstalledApp, texts: ScriptTexts) = """
        @chcp 65001 >nul
        @echo off
        title ${texts.title}
        echo ${texts.waiting}
        :wait
        tasklist /fi "PID eq $pid" | find "$pid" >nul && (timeout /t 1 /nobreak >nul & goto wait)
        echo ${texts.copying}
        robocopy "$newApp" "${app.folder}" /MIR /R:10 /W:1 /NFL /NDL /NJH /NJS /NP >nul
        if errorlevel 8 (echo ${texts.failed} & timeout /t 10 >nul)
        start "" "${app.executable}"
        cd /d "%TEMP%"
        rmdir /s /q "$workDir" & exit
    """.trimIndent().replace("\n", "\r\n") + "\r\n"

    /** Сообщения в окне консоли. В них нельзя скобки, `&`, `<`, `>` и `|`: cmd считает их командами. */
    private class ScriptTexts(val title: String, val waiting: String, val copying: String, val failed: String)
}
