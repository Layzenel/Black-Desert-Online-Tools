package com.layzen.bdotools.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val WORK_DIR_PREFIX = "bdo-tools-update"
private const val ZIP_NAME = "BDO-Tools-portable.zip"
private const val EXTRACT_DIR = "app"

/**
 * Обновление настольной программы: при запуске один раз сверяет свою версию с последней сборкой на GitHub и,
 * если та новее, предлагает её. И установленную, и портативную копию обновляет одинаково: скачивает портативный
 * архив и заменяет им файлы программы, без установщика.
 * Ошибка сети при проверке просто прячет плашку: программа работает и без обновлений.
 */
class UpdateViewModel(
    private val app: InstalledApp? = InstalledApp.current(),
    private val source: UpdateSource = UpdateSource(),
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Hidden)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    init {
        val current = app
        if (current != null) {
            viewModelScope.launch {
                val latest = runCatchingIo { source.latest() } ?: return@launch
                if (latest.version > current.version) _state.value = UpdateState.Available(latest)
            }
        }
    }

    fun showNotes(open: Boolean) = _state.update { if (it is UpdateState.Available) it.copy(notesOpen = open) else it }

    fun dismiss() {
        _state.value = UpdateState.Hidden
    }

    /**
     * Скачивает и распаковывает архив новой версии и запускает замену файлов; программа при этом закроется
     * и запустится снова. Если в папку программы нельзя писать, обновление сразу считается неудачным.
     */
    fun install() {
        val current = app ?: return
        val info = when (val state = _state.value) {
            is UpdateState.Available -> state.info
            is UpdateState.Failed -> state.info
            else -> return
        }
        _state.value = UpdateState.Downloading(info, progress = null)
        viewModelScope.launch {
            val ok = runCatchingIo {
                check(isWritable(current.folder))
                val workDir = Files.createTempDirectory(WORK_DIR_PREFIX)
                val zip = workDir.resolve(ZIP_NAME)
                source.download(info.zipUrl, zip) { progress -> _state.value = UpdateState.Downloading(info, progress) }
                val newApp = withContext(Dispatchers.IO) {
                    UpdateArchive.extract(zip, workDir.resolve(EXTRACT_DIR), current.executable.fileName.toString())
                }
                UpdateInstaller.installAndExit(newApp, workDir, current)
            }
            if (ok == null) _state.value = UpdateState.Failed(info)
        }
    }

    /** Можно ли создать файл в [folder]: программу, поставленную в защищённую папку, так не обновить. */
    private suspend fun isWritable(folder: Path): Boolean = withContext(Dispatchers.IO) {
        runCatching { Files.delete(Files.createTempFile(folder, WORK_DIR_PREFIX, ".tmp")) }.isSuccess
    }

    /** Ошибки сети и диска превращает в `null`; отмену пропускает дальше. */
    private suspend fun <T> runCatchingIo(block: suspend () -> T): T? =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
