package com.layzen.bdotools.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Проверка новой версии сайта: при открытии и затем раз в [UPDATE_CHECK_INTERVAL] сравнивает версию открытой
 * страницы ([WEB_VERSION], её вшивает сборка) с выложенной. Если выложена другая — предлагает обновить страницу.
 * У сборки разработчика версии нет, и она ничего не проверяет.
 */
internal class WebUpdateViewModel : ViewModel() {

    private val _newVersion = MutableStateFlow<String?>(null)

    /** Выложенная версия, если она новее открытой и её не отложили; иначе `null`. */
    val newVersion: StateFlow<String?> = _newVersion.asStateFlow()

    private var postponed: String? = null

    init {
        val own = WEB_VERSION
        if (own != null) {
            viewModelScope.launch {
                while (true) {
                    val deployed = fetchDeployedVersion()
                    if (deployed != null && deployed != own && deployed != postponed) _newVersion.value = deployed
                    delay(UPDATE_CHECK_INTERVAL)
                }
            }
        }
    }

    /** Прячет плашку до следующей выложенной версии. */
    fun postpone() {
        postponed = _newVersion.value
        _newVersion.value = null
    }
}
