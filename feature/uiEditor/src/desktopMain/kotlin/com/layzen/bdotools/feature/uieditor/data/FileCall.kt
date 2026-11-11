package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.feature.uieditor.domain.error.SlotsError
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsException
import java.io.IOException
import java.nio.file.AccessDeniedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Выполняет работу с диском в фоновом потоке, чтобы окно не замирало, и переводит ошибки ввода-вывода
 * в [SlotsException], как это делает мост в браузере: нет прав — [SlotsError.Permission], остальное —
 * [SlotsError.Unknown] с текстом ошибки.
 */
internal suspend fun <T> fileCall(block: () -> T): T = withContext(Dispatchers.IO) {
    try {
        block()
    } catch (e: SlotsException) {
        throw e
    } catch (e: AccessDeniedException) {
        throw SlotsException(SlotsError.Permission, e.message)
    } catch (e: IOException) {
        throw SlotsException(SlotsError.Unknown, e.message ?: e::class.simpleName)
    }
}
