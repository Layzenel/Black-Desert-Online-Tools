package com.layzen.bdotools.feature.gameinfo.domain.graphics

import kotlinx.coroutines.flow.Flow

/** Версия DirectX игры по загруженным в её процесс библиотекам. Только настольная версия; в игру не вмешивается. */
interface GameDirectXSource {
    /**
     * Раз в секунду, пока поток собирают, — версия DirectX запущенной игры; `null` — игра не запущена или версию не
     * узнать (процесс не отдал список библиотек).
     */
    fun watch(): Flow<DirectXVersion?>
}
