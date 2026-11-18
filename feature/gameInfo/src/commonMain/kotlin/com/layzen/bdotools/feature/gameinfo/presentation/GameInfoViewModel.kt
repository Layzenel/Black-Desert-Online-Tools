package com.layzen.bdotools.feature.gameinfo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingInteractor
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings
import com.layzen.bdotools.feature.gameinfo.presentation.event.GameInfoEvent
import com.layzen.bdotools.feature.gameinfo.presentation.state.GameInfoState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Пинг игры поверх неё: настройки показа и замер. Пинг меряется, только пока его показывают, — без показа
 * подключения игры не трогаются.
 *
 * @property interactor пинг и его настройки
 */
class GameInfoViewModel(private val interactor: PingInteractor) : ViewModel() {
    private val mutableState = MutableStateFlow(GameInfoState())
    private var watcher: Job? = null

    /** Состояние вкладки и окна пинга. */
    val state: StateFlow<GameInfoState> = mutableState.asStateFlow()

    /** Может ли платформа мерить пинг (только настольная версия). */
    val supported: Boolean get() = interactor.supported

    init {
        viewModelScope.launch {
            val settings = interactor.settings()
            mutableState.update { it.copy(settings = settings) }
            updateWatching()
        }
    }

    /** Действие игрока. */
    fun onEvent(event: GameInfoEvent) {
        when (event) {
            GameInfoEvent.TogglePing -> change(save = true) { it.copy(shown = !it.shown) }.also { updateWatching() }
            GameInfoEvent.ToggleMove -> mutableState.update { it.copy(movable = !it.movable) }
            is GameInfoEvent.SetScale -> change(event.save) { it.copy(scale = event.percent) }
            is GameInfoEvent.SetOpacity -> change(event.save) { it.copy(opacity = event.percent) }
        }
    }

    /** Меняет настройки; сохраняет, если [save] (ползунок сохраняется, когда его отпустили). */
    private fun change(save: Boolean, change: (PingSettings) -> PingSettings) {
        mutableState.update { it.copy(settings = change(it.settings)) }
        val settings = mutableState.value.settings
        if (save) viewModelScope.launch { interactor.save(settings) }
    }

    /** Мерить, пока пинг показывают и платформа умеет; иначе остановить замер. */
    private fun updateWatching() {
        if (mutableState.value.settings.shown && interactor.supported) {
            if (watcher?.isActive != true) watcher = viewModelScope.launch { interactor.watch().collect { ping -> mutableState.update { it.copy(ping = ping) } } }
        } else {
            watcher?.cancel()
            watcher = null
            mutableState.update { it.copy(ping = null) }
        }
    }
}
