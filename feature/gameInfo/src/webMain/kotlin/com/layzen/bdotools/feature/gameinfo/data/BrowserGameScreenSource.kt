package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.grid.GameScreenSource

/** Браузер не видит файлов игры: сетка поверх игры есть только в настольной версии. */
internal class BrowserGameScreenSource : GameScreenSource {
    override suspend fun uiHeight(): Double? = null
}

internal actual fun createGameScreenSource(): GameScreenSource = BrowserGameScreenSource()
