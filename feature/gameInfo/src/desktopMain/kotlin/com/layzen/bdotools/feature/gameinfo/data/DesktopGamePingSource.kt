package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.ping.GamePingSource
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingReading
import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Tlhelp32
import com.sun.jna.platform.win32.WinDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Пинг игры: подключение процесса `BlackDesert64.exe` к игровому серверу ищется заново при каждой проверке (сервер
 * и адрес меняются при смене канала и переподключении). Задержка — из статистики Windows, как в «Мониторе ресурсов»:
 * сбор для нового подключения включается сам, если программа запущена от администратора. Время установки своего
 * TCP-соединения не годится: VPN и ускорители отвечают на него сами, у игрока на компьютере (выходит 1 мс).
 */
internal class DesktopGamePingSource : GamePingSource {
    override val supported: Boolean = System.getProperty("os.name").orEmpty().startsWith("Windows")

    override fun watch(): Flow<PingReading> = flow {
        while (true) {
            emit(runCatching { measure() }.getOrElse { PingReading() })
            delay(CHECK_MS)
        }
    }.flowOn(Dispatchers.IO)

    private fun measure(): PingReading {
        val pid = gamePid() ?: return PingReading()
        val server = gameServer(tcpConnections(pid)) ?: return PingReading()
        // после загрузочного экрана игра подключается заново, и сбор задержки для нового подключения надо включить
        if (!isRttCollected(server) && !enableRtt(server)) return PingReading(needsAdmin = true)
        return PingReading(smoothedRtt(server))
    }

    /** Игровой сервер среди подключений: порт [GAME_PORT], если есть, иначе любое не веб-подключение. */
    private fun gameServer(connections: List<TcpConnection>): TcpConnection? =
        connections.firstOrNull { it.remotePort == GAME_PORT } ?: connections.firstOrNull { it.remotePort !in WEB_PORTS }

    /** Процесс игры по снимку списка процессов: имя exe видно и у процесса, запущенного от администратора. */
    private fun gamePid(): Long? {
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

    private companion object {
        const val CHECK_MS = 1000L
        const val GAME_EXE = "blackdesert64.exe"

        /** Порт игрового сервера, замер 2026-11-19: у него и считает задержку «Монитор ресурсов». */
        const val GAME_PORT = 8889
        val WEB_PORTS = setOf(80, 443)
    }
}

internal actual fun createGamePingSource(): GamePingSource = DesktopGamePingSource()
