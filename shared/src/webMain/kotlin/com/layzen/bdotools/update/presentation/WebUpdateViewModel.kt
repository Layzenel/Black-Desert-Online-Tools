package com.layzen.bdotools.update.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.update.domain.SiteUpdateInteractor
import com.layzen.bdotools.update.domain.UPDATE_CHECK_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Проверка новой версии сайта: при открытии и затем раз в [UPDATE_CHECK_INTERVAL] спрашивает у [interactor],
 * выложена ли версия, отличная от открытой. Если да — предлагает обновить страницу.
 * У сборки разработчика версии нет, и она ничего не проверяет.
 */
internal class WebUpdateViewModel(private val interactor: SiteUpdateInteractor) : ViewModel() {

    private val _newVersion = MutableStateFlow<String?>(null)

    /** Выложенная версия, если она новее открытой и её не отложили; иначе `null`. */
    val newVersion: StateFlow<String?> = _newVersion.asStateFlow()

    /** Версия открытой страницы для подписи в верхней навигации; `null` — сборка разработчика. */
    val ownVersion: String? get() = interactor.ownVersion

    private var postponed: String? = null

    init {
        if (interactor.ownVersion != null) {
            viewModelScope.launch {
                while (true) {
                    val deployed = interactor.newVersion()
                    if (deployed != null && deployed != postponed) _newVersion.value = deployed
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
