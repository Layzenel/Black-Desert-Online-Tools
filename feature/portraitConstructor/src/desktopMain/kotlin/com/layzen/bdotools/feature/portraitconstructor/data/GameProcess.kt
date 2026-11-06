package com.layzen.bdotools.feature.portraitconstructor.data

/** Клиент игры: `BlackDesert64.exe`, у старых клиентов — `BlackDesert32.exe`. */
private val GAME_PROCESS = Regex("""BlackDesert(32|64)\.exe""", RegexOption.IGNORE_CASE)

/**
 * Запущен ли клиент игры. Процесс игры защищён античитом: `ProcessHandle` не видит его путь,
 * поэтому имена процессов берутся из `tasklist` (строки CSV `"имя.exe","PID",…`).
 */
internal fun isGameProcessRunning(): Boolean {
    val process = ProcessBuilder("tasklist", "/FO", "CSV", "/NH").redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    process.waitFor()
    return output.lineSequence().any { GAME_PROCESS.matches(it.substringBefore(',').trim('"')) }
}
