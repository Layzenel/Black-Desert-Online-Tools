package com.layzen.bdotools.feature.comboassist.data.history

import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Журнал слежения за историей умений: что увидено на экране и как узнано. Файл
 * `BDO Tools\comboAssist\watch.log` папки игры, начинается заново при каждом запуске слежения; рядом — картинки
 * иконок, которые не удалось узнать (`miss-<мс>.png`), их тоже стирает новый запуск. Ошибки записи проглатываются:
 * журнал не должен мешать слежению.
 *
 * @property file файл журнала или `null`, если папки игры нет
 */
internal class WatchLog(private val file: Path?) {

    init {
        write { log ->
            Files.deleteIfExists(log)
            if (Files.isDirectory(log.parent)) log.parent.listDirectoryEntries("$MISS_PREFIX*.png").forEach(Files::deleteIfExists)
        }
    }

    /** Строка журнала с моментом [ms] от начала слежения. */
    fun line(ms: Long, text: String) = write {
        Files.createDirectories(it.parent)
        Files.writeString(it, "${ms.toString().padStart(TIME_WIDTH)} $text\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    /** Иконка [image] строки истории, которую не узнали в момент [ms]: `miss-<ms>.png` рядом с журналом. */
    fun miss(ms: Long, image: BufferedImage) = write {
        Files.createDirectories(it.parent)
        val target = it.parent.resolve("$MISS_PREFIX$ms.png")
        ImageIO.write(image, "png", target.toFile())
        line(ms, "saved ${target.name}")
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
        const val MISS_PREFIX = "miss-"
    }
}
