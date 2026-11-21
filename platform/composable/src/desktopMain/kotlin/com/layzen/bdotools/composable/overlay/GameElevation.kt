package com.layzen.bdotools.composable.overlay

import com.sun.jna.Structure
import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinError
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference

/**
 * Игра на переднем плане запущена от имени администратора (повышенный токен). Токен повышенного процесса
 * программа без прав прочитать не может — отказ в доступе тоже значит «от администратора»: токен обычного
 * процесса того же пользователя открывается всегда. Игра не на переднем плане — `false`.
 */
fun isGameElevated(): Boolean {
    val pid = foregroundGamePid() ?: return false
    val process = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_LIMITED_INFORMATION, false, pid) ?: return false
    try {
        val token = WinNT.HANDLEByReference()
        if (!Advapi32.INSTANCE.OpenProcessToken(process, WinNT.TOKEN_QUERY, token)) {
            return Kernel32.INSTANCE.GetLastError() == WinError.ERROR_ACCESS_DENIED
        }
        try {
            val elevation = TokenElevation()
            val size = IntByReference()
            val read = Advapi32.INSTANCE.GetTokenInformation(
                token.value, WinNT.TOKEN_INFORMATION_CLASS.TokenElevation, elevation, elevation.size(), size,
            )
            return read && elevation.TokenIsElevated != 0
        } finally {
            Kernel32.INSTANCE.CloseHandle(token.value)
        }
    } finally {
        Kernel32.INSTANCE.CloseHandle(process)
    }
}

/** Структура `TOKEN_ELEVATION` из WinAPI: ненулевое поле — токен повышен. */
@Structure.FieldOrder("TokenIsElevated")
internal class TokenElevation : Structure() {
    @JvmField var TokenIsElevated: Int = 0
}
