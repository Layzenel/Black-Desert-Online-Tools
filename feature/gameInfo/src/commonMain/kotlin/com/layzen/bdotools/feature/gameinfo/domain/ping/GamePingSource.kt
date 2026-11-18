package com.layzen.bdotools.feature.gameinfo.domain.ping

import kotlinx.coroutines.flow.Flow

/**
 * Пинг игры до сервера: задержка TCP-подключения процесса игры, как в «Мониторе ресурсов». Только настольная
 * версия; в игру не вмешивается.
 */
interface GamePingSource {
    /** Умеет ли платформа мерить пинг игры. */
    val supported: Boolean

    /** Замер пинга раз в секунду, пока поток собирают. */
    fun watch(): Flow<PingReading>
}
