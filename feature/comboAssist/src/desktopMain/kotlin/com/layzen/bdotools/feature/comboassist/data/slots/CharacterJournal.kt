package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.composable.overlay.foregroundGameFolder
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/** Файл журнала персонажа: `<CharacterNo>_<ггггмм>.bcf`. */
private val JOURNAL_FILE = Regex("""(\d+)_\d{6}\.bcf""")

/**
 * Персонаж, которым сейчас играют, по журналу игры: `<папка установки>\Cache\11\MyJournal\<CharacterNo>_<ггггмм>.bcf`.
 * Игра пишет файл персонажа через ≈10 с после его входа в мир — и при первом входе после запуска, и при смене персонажа,
 * а по ходу игры дописывает журнал текущего персонажа, поэтому самый свежий файл — тот, кем играют.
 * Папка установки — по процессу игры на переднем плане; найденная запоминается, пока программа работает.
 */
internal object CharacterJournal {
    @Volatile private var gameFolder: Path? = null

    /** Номер (`CharacterNo`) персонажа, которым сейчас играют, или `null`, если журнала нет. */
    fun current(): String? {
        foregroundGameFolder()?.let { gameFolder = it }
        val dir = gameFolder?.resolve("Cache")?.resolve("11")?.resolve("MyJournal")?.takeIf { it.isDirectory() } ?: return null
        val newest = try {
            dir.listDirectoryEntries("*.bcf").maxByOrNull { it.getLastModifiedTime() }
        } catch (e: IOException) {
            null
        }
        return newest?.let { characterNo(it.name) }
    }

    /** Номер персонажа из имени файла журнала [fileName] или `null`, если это не файл журнала. */
    fun characterNo(fileName: String): String? = JOURNAL_FILE.matchEntire(fileName)?.groupValues?.get(1)
}
