package com.layzen.bdotools.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.nio.file.Files
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Обновление настольной программы: при запуске один раз сверяет свою версию с последней сборкой на GitHub и,
 * если та новее, предлагает её. Установленную копию обновляет установщиком, портативную — ссылкой на новый ZIP.
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
                if (latest.version > current.version) _state.value = UpdateState.Available(latest, current.isPortable)
            }
        }
    }

    fun showNotes(open: Boolean) = _state.update { if (it is UpdateState.Available) it.copy(notesOpen = open) else it }

    fun dismiss() {
        _state.value = UpdateState.Hidden
    }

    /** Скачивает установщик и ставит его; программа при этом закроется и запустится снова. */
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
                val msi = Files.createTempDirectory("bdo-tools-update").resolve("BDO-Tools.msi")
                source.download(info.msiUrl, msi) { progress -> _state.value = UpdateState.Downloading(info, progress) }
                UpdateInstaller.installAndExit(msi, current)
            }
            if (ok == null) _state.value = UpdateState.Failed(info, current.isPortable)
        }
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
