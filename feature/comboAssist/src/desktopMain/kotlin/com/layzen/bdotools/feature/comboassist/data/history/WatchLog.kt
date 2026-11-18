package com.layzen.bdotools.feature.comboassist.data.history

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Журнал слежения за историей умений: что увидено на экране и как узнано. Файл
 * `BDO Tools\comboAssist\watch.log` папки игры, начинается заново при каждом запуске слежения. Ошибки записи
 * проглатываются: журнал не должен мешать слежению.
 *
 * @property file файл журнала или `null`, если папки игры нет
 */
internal class WatchLog(private val file: Path?) {

    init {
        write { Files.deleteIfExists(it) }
    }

    /** Строка журнала с моментом [ms] от начала слежения. */
    fun line(ms: Long, text: String) = write {
        Files.createDirectories(it.parent)
        Files.writeString(it, "${ms.toString().padStart(TIME_WIDTH)} $text\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    private fun write(action: (Path) -> Unit) {
        val path = file ?: return
        try {
            action(path)
        } catch (e: IOException) {
            // журнал необязателен
        }
    }

    private companion object {
        const val TIME_WIDTH = 7
    }
}
