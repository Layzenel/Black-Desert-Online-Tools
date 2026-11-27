package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.composable.overlay.foregroundGameFolder
import java.nio.file.Path

/**
 * Папка установки игры — по процессу игры на переднем плане. Найденная запоминается, пока программа работает:
 * игру сворачивают, а её журнал и лог нужны и тогда.
 */
internal object GameInstall {
    @Volatile private var folder: Path? = null

    /** Папка установки игры или `null`, если игру ещё ни разу не видели на переднем плане. */
    fun folder(): Path? {
        foregroundGameFolder()?.let { folder = it }
        return folder
    }
}
