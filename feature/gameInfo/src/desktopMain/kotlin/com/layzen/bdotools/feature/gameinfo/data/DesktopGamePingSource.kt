package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.ping.GamePingSource
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingReading
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
        val pid = gameProcessId() ?: return PingReading()
        val server = gameServer(tcpConnections(pid)) ?: return PingReading()
        // после загрузочного экрана игра подключается заново, и сбор задержки для нового подключения надо включить
        if (!isRttCollected(server) && !enableRtt(server)) return PingReading(needsAdmin = true)
        return PingReading(smoothedRtt(server))
    }

    /** Игровой сервер среди подключений: порт [GAME_PORT], если есть, иначе любое не веб-подключение. */
    private fun gameServer(connections: List<TcpConnection>): TcpConnection? =
        connections.firstOrNull { it.remotePort == GAME_PORT } ?: connections.firstOrNull { it.remotePort !in WEB_PORTS }

    private companion object {
        const val CHECK_MS = 1000L

        /** Порт игрового сервера, замер 2026-11-19: у него и считает задержку «Монитор ресурсов». */
        const val GAME_PORT = 8889
        val WEB_PORTS = setOf(80, 443)
    }
}

internal actual fun createGamePingSource(): GamePingSource = DesktopGamePingSource()
