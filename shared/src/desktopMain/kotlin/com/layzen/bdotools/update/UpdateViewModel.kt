package com.layzen.bdotools.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
 * Обновление настольной программы: при запуске и затем раз в [UPDATE_CHECK_INTERVAL] сверяет свою версию
 * с последней сборкой на GitHub и, если та новее, предлагает её. Проверить можно и вручную ([checkNow]).
 * И установленную, и портативную копию обновляет одинаково: скачивает портативный архив и заменяет им файлы
 * программы, без установщика. Ошибка сети при автоматической проверке ничего не показывает: программа
 * работает и без обновлений.
 */
class UpdateViewModel(
    private val app: InstalledApp? = InstalledApp.current(),
    private val source: UpdateSource = UpdateSource(),
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Hidden)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val _checkStatus = MutableStateFlow(UpdateCheckStatus.Idle)
    val checkStatus: StateFlow<UpdateCheckStatus> = _checkStatus.asStateFlow()

    /** Версия запущенной программы; `null`, если она запущена не из пакета и обновлять её нечем. */
    val version: AppVersion? get() = app?.version

    init {
        if (app != null) {
            viewModelScope.launch {
                while (true) {
                    refresh()
                    delay(UPDATE_CHECK_INTERVAL)
                }
            }
        }
    }

    /** Ручная проверка: если новая версия есть, сразу открывает предложение обновиться, иначе пишет итог. */
    fun checkNow() {
        if (_checkStatus.value == UpdateCheckStatus.Checking) return
        _checkStatus.value = UpdateCheckStatus.Checking
        viewModelScope.launch {
            val found = refresh()
            _checkStatus.value = when (found) {
                null -> UpdateCheckStatus.Failed
                false -> UpdateCheckStatus.UpToDate
                true -> UpdateCheckStatus.Idle
            }
            if (found == true) showNotes(true)
        }
    }

    fun clearCheckStatus() {
        _checkStatus.value = UpdateCheckStatus.Idle
    }

    fun showNotes(open: Boolean) = _state.update { if (it is UpdateState.Available) it.copy(notesOpen = open) else it }

    /** Убирает ошибку обновления; кнопка «Обновить до …» в навигации остаётся. */
    fun postpone() = _state.update { if (it is UpdateState.Failed) UpdateState.Available(it.info) else it }

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

    /**
     * Читает последнюю сборку: `true` — есть новее, `false` — нет, `null` — не удалось узнать.
     * Идущее скачивание и его ошибку не трогает; уже предложенную версию заменяет только более новой.
     */
    private suspend fun refresh(): Boolean? {
        val current = app ?: return null
        val latest = runCatchingIo { source.latest() } ?: return null
        if (latest.version <= current.version) return false
        _state.update {
            when {
                it == UpdateState.Hidden -> UpdateState.Available(latest)
                it is UpdateState.Available && it.info.version < latest.version -> it.copy(info = latest)
                else -> it
            }
        }
        return true
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
