package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsException
import java.io.IOException
import java.nio.file.AccessDeniedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Выполняет работу с диском в фоновом потоке, чтобы окно не замирало, и переводит ошибки ввода-вывода
 * в [PortraitsException], как это делает мост в браузере: нет прав — [PortraitsError.Permission], остальное —
 * [PortraitsError.Unknown] с текстом ошибки.
 */
internal suspend fun <T> fileCall(block: () -> T): T = withContext(Dispatchers.IO) {
    try {
        block()
    } catch (e: PortraitsException) {
        throw e
    } catch (e: AccessDeniedException) {
        throw PortraitsException(PortraitsError.Permission, e.message)
    } catch (e: IOException) {
        throw PortraitsException(PortraitsError.Unknown, e.message ?: e::class.simpleName)
    }
}
