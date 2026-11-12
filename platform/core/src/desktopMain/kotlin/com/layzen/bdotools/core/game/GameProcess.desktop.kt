package com.layzen.bdotools.core.game

/** Имена исполняемых файлов клиента игры в нижнем регистре. */
private val GAME_EXECUTABLES = setOf("blackdesert64.exe", "blackdesert32.exe")

/** Ищет клиент игры среди процессов пользователя; если список процессов недоступен — `null`. */
actual fun isGameRunning(): Boolean? = try {
    ProcessHandle.allProcesses().anyMatch { process ->
        val command = process.info().command().orElse("")
        command.substringAfterLast('\\').substringAfterLast('/').lowercase() in GAME_EXECUTABLES
    }
} catch (e: SecurityException) {
    null
}
