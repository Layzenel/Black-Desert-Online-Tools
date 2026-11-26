package com.layzen.bdotools.composable.overlay

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import java.nio.file.Path
import kotlin.io.path.Path

private const val GAME_EXE = "blackdesert64.exe"
private const val PATH_LENGTH = 1024

/**
 * Окно игры на переднем плане. Путь процесса читается через `QueryFullProcessImageName` с правами «только узнать
 * имя»: так он доступен, даже если игра запущена от администратора. Не Windows — всегда `true`.
 */
fun isGameForeground(): Boolean {
    if (!System.getProperty("os.name").orEmpty().startsWith("Windows")) return true
    return foregroundGamePid() != null
}

/**
 * Папка установки игры (над `bin64\BlackDesert64.exe`), если её окно на переднем плане, иначе `null`. Путь процесса
 * игры, запущенной от администратора, виден только программе с теми же правами. Только Windows.
 */
fun foregroundGameFolder(): Path? = foregroundGame()?.second?.let { Path(it).parent?.parent }

/** Номер процесса игры, если её окно на переднем плане, иначе `null`. Только Windows. */
internal fun foregroundGamePid(): Int? = foregroundGame()?.first

/** Номер и путь процесса игры, если её окно на переднем плане. */
private fun foregroundGame(): Pair<Int, String>? {
    val window = User32.INSTANCE.GetForegroundWindow() ?: return null
    val pid = IntByReference()
    User32.INSTANCE.GetWindowThreadProcessId(window, pid)
    val process = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_LIMITED_INFORMATION, false, pid.value) ?: return null
    try {
        val buffer = CharArray(PATH_LENGTH)
        val size = IntByReference(buffer.size)
        if (!Kernel32.INSTANCE.QueryFullProcessImageName(process, 0, buffer, size)) return null
        val path = String(buffer, 0, size.value)
        return (pid.value to path).takeIf { path.lowercase().endsWith(GAME_EXE) }
    } finally {
        Kernel32.INSTANCE.CloseHandle(process)
    }
}
