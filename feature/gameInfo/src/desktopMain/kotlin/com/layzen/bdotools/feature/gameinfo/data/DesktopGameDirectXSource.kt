package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.graphics.DirectXVersion
import com.layzen.bdotools.feature.gameinfo.domain.graphics.GameDirectXSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Версия DirectX игры `BlackDesert64.exe` по библиотекам процесса: `d3d12.dll` — DX12, иначе `d3d11.dll` — DX11.
 * Список библиотек читается один раз на процесс игры; пока версию узнать не удалось (игра ещё грузится или не отдаёт
 * список), проверка повторяется раз в секунду.
 */
internal class DesktopGameDirectXSource : GameDirectXSource {
    override fun watch(): Flow<DirectXVersion?> = flow {
        var processId: Long? = null
        var version: DirectXVersion? = null
        while (true) {
            val current = runCatching { gameProcessId() }.getOrNull()
            if (current != processId || (current != null && version == null)) {
                processId = current
                version = current?.let { runCatching { directXOf(it) }.getOrNull() }
            }
            emit(version)
            delay(CHECK_MS)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    private fun directXOf(processId: Long): DirectXVersion? {
        val modules = loadedModules(processId) ?: return null
        return when {
            "d3d12.dll" in modules -> DirectXVersion.DirectX12
            "d3d11.dll" in modules -> DirectXVersion.DirectX11
            else -> null
        }
    }

    private companion object {
        const val CHECK_MS = 1000L
    }
}

internal actual fun createGameDirectXSource(): GameDirectXSource = DesktopGameDirectXSource()
