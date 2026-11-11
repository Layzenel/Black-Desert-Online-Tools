package com.layzen.bdotools.update.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.update.domain.AppVersion
import com.layzen.bdotools.update.domain.UPDATE_CHECK_INTERVAL
import com.layzen.bdotools.update.domain.UpdateInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Обновление настольной программы: при запуске и затем раз в [UPDATE_CHECK_INTERVAL] сверяет свою версию
 * с последней сборкой на GitHub и, если та новее, предлагает её. Проверить можно и вручную ([checkNow]).
 * Саму проверку и установку делает [UpdateInteractor]. Ошибка сети при автоматической проверке ничего не показывает: программа
 * работает и без обновлений.
 */
class UpdateViewModel(private val interactor: UpdateInteractor) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Hidden)
    /** Что показывать про обновление: ничего, предложение новой версии, ход загрузки или ошибку. */
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val _checkStatus = MutableStateFlow(UpdateCheckStatus.Idle)
    /** Итог ручной проверки ([checkNow]) для сообщения рядом с версией. */
    val checkStatus: StateFlow<UpdateCheckStatus> = _checkStatus.asStateFlow()

    /** Версия запущенной программы; `null`, если она запущена не из пакета и обновлять её нечем. */
    val version: AppVersion? get() = interactor.version

    init {
        if (interactor.version != null) {
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

    /** Сбрасывает итог ручной проверки, когда сообщение о нём закрыто. */
    fun clearCheckStatus() {
        _checkStatus.value = UpdateCheckStatus.Idle
    }

    /** Открывает или закрывает окно «Что нового» у предложенной версии. */
    fun showNotes(open: Boolean) = _state.update { if (it is UpdateState.Available) it.copy(notesOpen = open) else it }

    /** Убирает ошибку обновления; кнопка «Обновить до …» в навигации остаётся. */
    fun postpone() = _state.update { if (it is UpdateState.Failed) UpdateState.Available(it.info) else it }

    /**
     * Скачивает и распаковывает архив новой версии и запускает замену файлов; программа при этом закроется
     * и запустится снова. Если в папку программы нельзя писать, обновление сразу считается неудачным.
     */
    fun install() {
        if (interactor.version == null) return
        val info = when (val state = _state.value) {
            is UpdateState.Available -> state.info
            is UpdateState.Failed -> state.info
            else -> return
        }
        _state.value = UpdateState.Downloading(info, progress = null)
        viewModelScope.launch {
            val ok = runCatchingIo {
                interactor.install(info) { progress -> _state.value = UpdateState.Downloading(info, progress) }
            }
            if (ok == null) _state.value = UpdateState.Failed(info)
        }
    }

    /**
     * Читает последнюю сборку: `true` — есть новее, `false` — нет, `null` — не удалось узнать.
     * Идущее скачивание и его ошибку не трогает; уже предложенную версию заменяет только более новой.
     */
    private suspend fun refresh(): Boolean? {
        if (interactor.version == null) return null
        val latest = runCatchingIo { interactor.latest() } ?: return null
        if (!interactor.isNewer(latest)) return false
        _state.update {
            when {
                it == UpdateState.Hidden -> UpdateState.Available(latest)
                it is UpdateState.Available && it.info.version < latest.version -> it.copy(info = latest)
                else -> it
            }
        }
        return true
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
