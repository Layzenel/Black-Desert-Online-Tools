package com.layzen.bdotools.feature.gameinfo.domain.fps

import kotlinx.coroutines.flow.Flow

/**
 * FPS игры: сколько кадров в секунду игра отдаёт на экран, по системным событиям Windows о показе кадров (ETW), как
 * Xbox Game Bar и PresentMon. Только настольная версия; в игру не вмешивается.
 */
interface GameFpsSource {
    /** Может ли FPS мерить эта платформа и эта программа: события о кадрах Windows отдаёт только администратору. */
    val available: Boolean

    /** FPS раз в секунду, пока поток собирают; `null` — игра не запущена или не рисует кадры. */
    fun watch(): Flow<Int?>
}
