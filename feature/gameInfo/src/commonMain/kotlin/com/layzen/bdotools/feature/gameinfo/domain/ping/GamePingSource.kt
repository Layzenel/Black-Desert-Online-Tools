package com.layzen.bdotools.feature.gameinfo.domain.ping

import kotlinx.coroutines.flow.Flow

/**
 * Пинг игры до сервера: задержка TCP-подключения процесса игры, как в «Мониторе ресурсов». Только настольная
 * версия; в игру не вмешивается.
 */
interface GamePingSource {
    /** Умеет ли платформа мерить пинг игры. */
    val supported: Boolean

    /** Пинг в миллисекундах раз в секунду, пока поток собирают; `null` — игра не запущена или пинг не узнать. */
    fun watch(): Flow<Int?>
}
