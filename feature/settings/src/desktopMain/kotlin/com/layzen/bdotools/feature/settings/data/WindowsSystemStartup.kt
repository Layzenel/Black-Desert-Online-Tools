package com.layzen.bdotools.feature.settings.data

import com.layzen.bdotools.composable.overlay.isRunningAsAdmin
import com.layzen.bdotools.core.folder.GameFolderSettings
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import java.util.concurrent.TimeUnit

private const val RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"
private const val LAYERS_KEY = "Software\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Layers"
private const val RUN_AS_ADMIN = "RUNASADMIN"
private const val ENTRY_NAME = "BDO Tools"
private const val COMMAND_TIMEOUT_S = 60L

/**
 * Автозапуск и запуск от администратора в реестре текущего пользователя и планировщике Windows.
 *
 * - «От имени администратора» — флаг совместимости `~ RUNASADMIN` у exe (`AppCompatFlags\Layers`), как галочка
 *   на вкладке «Совместимость» свойств файла: Windows спрашивает UAC при каждом запуске.
 * - Обычный автозапуск — строка в `Run`. Такие строки Windows с правами администратора не запускает, поэтому
 *   автозапуск от администратора — задача планировщика «при входе» с наивысшими правами: она стартует без вопроса UAC.
 *   Создать или удалить её может только администратор: без прав команда идёт через `Start-Process -Verb RunAs`.
 *
 * @property exe путь к `BDO Tools.exe` из `jpackage.app-path`; `null` при запуске из Gradle — тогда настроек нет
 */
internal class WindowsSystemStartup(private val exe: String?) : SystemStartup {
    override val supported: Boolean = exe != null

    override suspend fun launchesAtStartup(): Boolean = io { runEntry() != null || taskExists() }

    override suspend fun runsAsAdmin(): Boolean = io { layersFlags()?.contains(RUN_AS_ADMIN) == true }

    override suspend fun setRunsAsAdmin(enabled: Boolean) = io {
        val path = exe ?: return@io
        if (enabled) {
            Advapi32Util.registryCreateKey(HKEY_CURRENT_USER, LAYERS_KEY)
            Advapi32Util.registrySetStringValue(HKEY_CURRENT_USER, LAYERS_KEY, path, "~ $RUN_AS_ADMIN")
        } else if (layersFlags() != null) {
            Advapi32Util.registryDeleteValue(HKEY_CURRENT_USER, LAYERS_KEY, path)
        }
    }

    // не реестр Windows, а настройки программы: их читает окно при запуске (`DesktopApp`)
    override suspend fun startsInTray(): Boolean = io { GameFolderSettings().startInTray }

    override suspend fun setStartsInTray(enabled: Boolean) = io { GameFolderSettings().startInTray = enabled }

    override suspend fun setLaunchAtStartup(enabled: Boolean, elevated: Boolean) = io {
        val path = exe ?: return@io
        if (enabled && !elevated) {
            Advapi32Util.registrySetStringValue(HKEY_CURRENT_USER, RUN_KEY, ENTRY_NAME, "\"$path\"")
        } else if (runEntry() != null) {
            Advapi32Util.registryDeleteValue(HKEY_CURRENT_USER, RUN_KEY, ENTRY_NAME)
        }
        when {
            enabled && elevated -> runPowerShellAsAdmin(registerTaskScript(path))
            taskExists() -> runPowerShellAsAdmin("Unregister-ScheduledTask -TaskName '$ENTRY_NAME' -Confirm:\$false")
        }
    }

    private fun runEntry(): String? = readValue(RUN_KEY, ENTRY_NAME)

    private fun layersFlags(): String? = exe?.let { readValue(LAYERS_KEY, it) }

    private fun readValue(key: String, name: String): String? = runCatching {
        if (Advapi32Util.registryValueExists(HKEY_CURRENT_USER, key, name)) Advapi32Util.registryGetStringValue(HKEY_CURRENT_USER, key, name) else null
    }.getOrNull()

    private fun taskExists(): Boolean = run(listOf("schtasks", "/Query", "/TN", ENTRY_NAME)) == 0

    /** Задача «при входе текущего пользователя» с наивысшими правами, без ограничения времени работы и питания. */
    private fun registerTaskScript(path: String): String {
        val exePath = path.replace("'", "''")
        return """
            ${'$'}action = New-ScheduledTaskAction -Execute '$exePath'
            ${'$'}trigger = New-ScheduledTaskTrigger -AtLogOn -User ${'$'}env:USERNAME
            ${'$'}principal = New-ScheduledTaskPrincipal -UserId ${'$'}env:USERNAME -LogonType Interactive -RunLevel Highest
            ${'$'}settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit 0
            Register-ScheduledTask -TaskName '$ENTRY_NAME' -Action ${'$'}action -Trigger ${'$'}trigger -Principal ${'$'}principal -Settings ${'$'}settings -Force
        """.trimIndent()
    }

    /** Выполняет [script] с правами администратора: сразу, если они уже есть, иначе — через окно UAC. */
    private fun runPowerShellAsAdmin(script: String) {
        val encoded = encode(script)
        if (isRunningAsAdmin()) {
            run(listOf("powershell", "-NoProfile", "-NonInteractive", "-EncodedCommand", encoded))
            return
        }
        val elevate = "Start-Process powershell -Verb RunAs -Wait -WindowStyle Hidden " +
            "-ArgumentList '-NoProfile','-NonInteractive','-EncodedCommand','$encoded'"
        run(listOf("powershell", "-NoProfile", "-NonInteractive", "-EncodedCommand", encode(elevate)))
    }

    /** Текст команды PowerShell для `-EncodedCommand`: Base64 от UTF-16LE — без забот о кавычках и пробелах в пути. */
    private fun encode(script: String): String = Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE))

    /** Запускает команду без окна и ждёт её; код выхода или `-1`, если она не запустилась или не уложилась во время. */
    private fun run(command: List<String>): Int = runCatching {
        val process = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start()
        if (process.waitFor(COMMAND_TIMEOUT_S, TimeUnit.SECONDS)) process.exitValue() else process.destroy().let { -1 }
    }.getOrDefault(-1)

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }
}

internal actual fun createSystemStartup(): SystemStartup = WindowsSystemStartup(System.getProperty("jpackage.app-path"))
