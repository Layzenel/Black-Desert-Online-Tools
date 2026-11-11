package com.layzen.bdotools.update.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Как часто приложение само проверяет, не вышла ли новая версия. Сборка выходит после каждого влития в `main`,
 * то есть не чаще нескольких раз в день: полчаса не нагружают GitHub и не дают долго сидеть на старой версии.
 */
internal val UPDATE_CHECK_INTERVAL: Duration = 30.minutes
