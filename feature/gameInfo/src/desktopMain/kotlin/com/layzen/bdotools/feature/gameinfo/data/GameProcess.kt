package com.layzen.bdotools.feature.gameinfo.data

import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Tlhelp32
import com.sun.jna.platform.win32.WinDef

private const val GAME_EXE = "blackdesert64.exe"

/** Номер процесса игры по снимку списка процессов: имя exe видно и у процесса, запущенного от администратора. */
internal fun gameProcessId(): Long? {
    val snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, WinDef.DWORD(0))
    try {
        val entry = Tlhelp32.PROCESSENTRY32.ByReference()
        var found = Kernel32.INSTANCE.Process32First(snapshot, entry)
        while (found) {
            if (Native.toString(entry.szExeFile).equals(GAME_EXE, ignoreCase = true)) return entry.th32ProcessID.toLong()
            found = Kernel32.INSTANCE.Process32Next(snapshot, entry)
        }
        return null
    } finally {
        Kernel32.INSTANCE.CloseHandle(snapshot)
    }
}
