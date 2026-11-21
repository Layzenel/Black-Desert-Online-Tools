package com.layzen.bdotools.composable.overlay

import com.sun.jna.Library
import com.sun.jna.Native

/** Права администратора у процесса — `IsUserAnAdmin` из shell32 (повышенный токен под UAC); не Windows или ошибка — `false`. */
actual fun isRunningAsAdmin(): Boolean = runCatching { Native.load("shell32", ShellAdmin::class.java).IsUserAnAdmin() }.getOrDefault(false)

/** Функция shell32, которой нет в jna-platform. */
@Suppress("FunctionName")
private interface ShellAdmin : Library {
    fun IsUserAnAdmin(): Boolean
}
