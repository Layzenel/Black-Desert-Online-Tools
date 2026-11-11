package com.layzen.bdotools.update.data

import com.layzen.bdotools.update.domain.AppVersion
import com.layzen.bdotools.update.domain.UpdateInfo
import com.layzen.bdotools.update.domain.UpdateRepository
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val WORK_DIR_PREFIX = "bdo-tools-update"
private const val ZIP_NAME = "BDO-Tools-portable.zip"
private const val EXTRACT_DIR = "app"

/**
 * Обновление через GitHub: описание сборки и портативный архив скачивает [source], файлы программы [app]
 * заменяет [UpdateInstaller]. И установленную, и портативную копию обновляет одинаково, без установщика.
 */
class UpdateRepositoryImpl(
    private val app: InstalledApp?,
    private val source: UpdateSource,
) : UpdateRepository {

    override val currentVersion: AppVersion? get() = app?.version

    override suspend fun latest(): UpdateInfo? = source.latest()

    override suspend fun install(info: UpdateInfo, onProgress: (Float) -> Unit) {
        val current = checkNotNull(app)
        check(isWritable(current.folder))
        val workDir = Files.createTempDirectory(WORK_DIR_PREFIX)
        val zip = workDir.resolve(ZIP_NAME)
        source.download(info.zipUrl, zip, onProgress)
        val newApp = withContext(Dispatchers.IO) {
            UpdateArchive.extract(zip, workDir.resolve(EXTRACT_DIR), current.executable.fileName.toString())
        }
        UpdateInstaller.installAndExit(newApp, workDir, current)
    }

    /** Можно ли создать файл в [folder]: программу, поставленную в защищённую папку, так не обновить. */
    private suspend fun isWritable(folder: Path): Boolean = withContext(Dispatchers.IO) {
        runCatching { Files.delete(Files.createTempFile(folder, WORK_DIR_PREFIX, ".tmp")) }.isSuccess
    }
}
