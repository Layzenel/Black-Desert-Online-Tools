package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.core.folder.GameFolderSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Комбо в файле `BDO Tools\comboAssist\combos.json` папки игры. Комбо — про класс, а не про аккаунт, поэтому
 * папки аккаунта в пути нет. Запись — через временный файл, чтобы оборванная запись не испортила комбо.
 *
 * @property folder папка `Black Desert` или `null`, если её нет
 */
internal class DesktopComboStorage(private val folder: () -> Path?) : ComboStorage {

    override suspend fun read(): String? = withContext(Dispatchers.IO) {
        val file = file() ?: return@withContext null
        try {
            if (Files.exists(file)) Files.readString(file) else null
        } catch (e: IOException) {
            null
        }
    }

    override suspend fun write(text: String): Boolean = withContext(Dispatchers.IO) {
        val file = file() ?: return@withContext false
        try {
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling("${file.fileName}.tmp")
            Files.writeString(temp, text)
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun file(): Path? = folder()?.resolve("BDO Tools")?.resolve("comboAssist")?.resolve("combos.json")
}

internal actual fun createComboStorage(): ComboStorage = DesktopComboStorage { GameFolderSettings().folder }
