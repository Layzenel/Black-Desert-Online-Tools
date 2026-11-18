package com.layzen.bdotools.feature.gameinfo.domain.ping

/**
 * Замер пинга.
 *
 * @property ms пинг в миллисекундах; `null` — игра не запущена, замеров ещё нет или их не собрать
 * @property needsAdmin Windows не собирает задержку подключения игры, а включить сбор без прав администратора нельзя
 */
data class PingReading(val ms: Int? = null, val needsAdmin: Boolean = false)
