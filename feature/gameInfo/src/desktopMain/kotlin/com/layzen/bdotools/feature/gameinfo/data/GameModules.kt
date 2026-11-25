package com.layzen.bdotools.feature.gameinfo.data

import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Tlhelp32
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.platform.win32.WinDef

/** Флаги снимка: библиотеки процесса, в том числе 32-разрядные. */
private val SNAPSHOT_FLAGS = Tlhelp32.TH32CS_SNAPMODULE.toLong() or Tlhelp32.TH32CS_SNAPMODULE32.toLong()

/**
 * Имена (в нижнем регистре) библиотек, загруженных в процесс [processId], или `null`, если список не отдан
 * (защита игры или нет прав).
 */
internal fun loadedModules(processId: Long): Set<String>? {
    val snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(WinDef.DWORD(SNAPSHOT_FLAGS), WinDef.DWORD(processId))
    if (snapshot == null || snapshot == WinBase.INVALID_HANDLE_VALUE) return null
    try {
        val entry = Tlhelp32.MODULEENTRY32W.ByReference()
        val names = mutableSetOf<String>()
        var found = Kernel32.INSTANCE.Module32FirstW(snapshot, entry)
        while (found) {
            names += Native.toString(entry.szModule).lowercase()
            found = Kernel32.INSTANCE.Module32NextW(snapshot, entry)
        }
        return names.ifEmpty { null }
    } finally {
        Kernel32.INSTANCE.CloseHandle(snapshot)
    }
}
