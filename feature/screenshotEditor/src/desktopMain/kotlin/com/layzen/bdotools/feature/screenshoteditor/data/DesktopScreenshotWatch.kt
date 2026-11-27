package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.COMPLETE_TAIL_BYTES
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.PendingScreenshots
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.ScreenshotWatch
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.looksComplete
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchKey
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences
import kotlin.io.path.isDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val ALERTS_KEY = "alerts"

// Проверка раз в полсекунды: размер файла сравнивается между двумя проверками
private const val CHECK_MS = 500L

// Папки ScreenShot нет, пока игра не сделала первый скриншот: ждём её появления
private const val NO_FOLDER_MS = 30_000L

/**
 * Слежение за `Black Desert\ScreenShot` через [java.nio.file.WatchService] — система сама сообщает о новых файлах,
 * папка не перечитывается. Файл выдаётся, когда игра дописала его ([PendingScreenshots]). Включатель — в
 * `Preferences` пользователя ([preferences]).
 */
internal class DesktopScreenshotWatch(
    private val gameFolder: () -> Path?,
    private val preferences: Preferences,
) : ScreenshotWatch {
    override val supported = true

    override var alertsEnabled: Boolean
        get() = preferences.getBoolean(ALERTS_KEY, true)
        set(value) = preferences.putBoolean(ALERTS_KEY, value)

    override fun newScreenshots(): Flow<ScreenshotEntry> = flow {
        while (true) {
            val folder = gameFolder()?.resolve(SCREENSHOT_FOLDER)
            if (folder == null || !folder.isDirectory()) delay(NO_FOLDER_MS) else watch(folder)
        }
    }.flowOn(Dispatchers.IO)

    /** Следит за [folder], пока она есть; удалили — возвращается ждать её снова. */
    private suspend fun FlowCollector<ScreenshotEntry>.watch(folder: Path) {
        val service = FileSystems.getDefault().newWatchService()
        service.use {
            folder.register(service, ENTRY_CREATE, ENTRY_MODIFY)
            val pending = PendingScreenshots()
            while (true) {
                currentCoroutineContext().ensureActive()
                val key: WatchKey? = service.poll(CHECK_MS, TimeUnit.MILLISECONDS)
                val now = System.currentTimeMillis()
                key?.pollEvents()?.forEach { event -> (event.context() as? Path)?.let { pending.seen(it.toString(), now) } }
                if (key != null && !key.reset()) return
                pending.waiting.toList().forEach { name ->
                    val file = folder.resolve(name)
                    val (size, tail) = sizeAndTail(file)
                    if (pending.check(name, size, looksComplete(name, tail), now)) emit(ScreenshotEntry(file.toString(), name, now))
                }
            }
        }
    }

    /** Размер файла и его последние байты; пока игра держит файл открытым на запись, чтение может не пройти. */
    private fun sizeAndTail(file: Path): Pair<Long, ByteArray> = try {
        RandomAccessFile(file.toFile(), "r").use { raf ->
            val size = raf.length()
            val tail = ByteArray(minOf(size, COMPLETE_TAIL_BYTES.toLong()).toInt())
            raf.seek(size - tail.size)
            raf.readFully(tail)
            size to tail
        }
    } catch (e: IOException) {
        -1L to ByteArray(0)
    }
}
