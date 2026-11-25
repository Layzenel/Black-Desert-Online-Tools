package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.gameinfo.domain.grid.GameScreenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Размер экрана игры по файлу выбранного аккаунта в папке `Black Desert` ([accountText], [parseUiHeight]). */
internal class DesktopGameScreenSource : GameScreenSource {
    override suspend fun uiHeight(): Double? = withContext(Dispatchers.IO) {
        runCatching {
            val settings = GameFolderSettings()
            settings.folder?.let { accountText(it, settings.account) }?.let(::parseUiHeight)
        }.getOrNull()
    }
}

internal actual fun createGameScreenSource(): GameScreenSource = DesktopGameScreenSource()
