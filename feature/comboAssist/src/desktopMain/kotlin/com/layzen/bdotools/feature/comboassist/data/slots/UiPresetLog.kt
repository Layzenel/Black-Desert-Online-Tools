package com.layzen.bdotools.feature.comboassist.data.slots

import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Path
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

/**
 * Пресет интерфейса, выбранный в игре, по логу клиента: `<папка установки>\Log\Client_<дата>_<время>.json`
 * (по файлу на запуск, UTF-16 LE, игра дописывает его сразу). Файл аккаунта игра пишет только при смене персонажа,
 * телепорте и выходе, а выбор пресета виден в логе сразу ([PresetSelection]).
 * Лог дочитывается с места, где остановились в прошлый раз: за часы игры он вырастает до мегабайтов.
 */
internal object UiPresetLog {
    private var file: Path? = null
    private var offset = 0L
    private var selection = PresetSelection()

    /** Номер пресета (с нуля), выбранного в игре позже момента [millis], или `null`. */
    @Synchronized
    fun selectedAfter(millis: Long): Int? {
        readNew()
        return selection.selected?.takeIf { it.first > millis }?.second
    }

    /** Дочитывает новые строки самого свежего лога; новый лог (перезапуск игры) читается с начала. */
    private fun readNew() {
        val newest = newestLog() ?: return
        if (newest != file) {
            file = newest
            offset = 0
            selection = PresetSelection()
        }
        try {
            RandomAccessFile(newest.toFile(), "r").use { log ->
                val bytes = ByteArray((log.length() - offset).coerceAtLeast(0).toInt() and 1.inv())
                log.seek(offset)
                log.readFully(bytes)
                val lines = String(bytes, Charsets.UTF_16LE).split('\n')
                // последняя строка может быть недописанной — её дочитаем в следующий раз
                lines.dropLast(1).forEach(selection::feed)
                offset += bytes.size - lines.last().length * 2
            }
        } catch (e: IOException) {
            // лог занят или пропал — попробуем при следующей проверке
        }
    }

    /** Лог текущего (последнего) запуска игры или `null`, если папка установки не известна. */
    private fun newestLog(): Path? {
        val dir = GameInstall.folder()?.resolve("Log")?.takeIf { it.isDirectory() } ?: return null
        return try {
            dir.listDirectoryEntries("Client_*.json").maxByOrNull { it.getLastModifiedTime() }
        } catch (e: IOException) {
            null
        }
    }
}
