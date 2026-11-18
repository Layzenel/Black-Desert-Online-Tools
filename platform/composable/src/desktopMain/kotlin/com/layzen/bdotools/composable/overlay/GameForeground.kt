package com.layzen.bdotools.composable.overlay

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference

private const val GAME_EXE = "blackdesert64.exe"
private const val PATH_LENGTH = 1024

/**
 * Окно игры на переднем плане. Путь процесса читается через `QueryFullProcessImageName` с правами «только узнать
 * имя»: так он доступен, даже если игра запущена от администратора. Не Windows — всегда `true`.
 */
fun isGameForeground(): Boolean {
    if (!System.getProperty("os.name").orEmpty().startsWith("Windows")) return true
    val window = User32.INSTANCE.GetForegroundWindow() ?: return false
    val pid = IntByReference()
    User32.INSTANCE.GetWindowThreadProcessId(window, pid)
    val process = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_LIMITED_INFORMATION, false, pid.value) ?: return false
    try {
        val buffer = CharArray(PATH_LENGTH)
        val size = IntByReference(buffer.size)
        if (!Kernel32.INSTANCE.QueryFullProcessImageName(process, 0, buffer, size)) return false
        return String(buffer, 0, size.value).lowercase().endsWith(GAME_EXE)
    } finally {
        Kernel32.INSTANCE.CloseHandle(process)
    }
}
