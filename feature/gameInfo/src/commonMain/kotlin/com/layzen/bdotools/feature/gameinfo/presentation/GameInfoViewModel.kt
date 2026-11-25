package com.layzen.bdotools.feature.gameinfo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingInteractor
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings
import com.layzen.bdotools.feature.gameinfo.presentation.event.GameInfoEvent
import com.layzen.bdotools.feature.gameinfo.presentation.state.GameInfoState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Пинг и FPS игры поверх неё: настройки показа и замер. Каждое значение меряется, только пока его показывают, — без
 * показа подключения и кадры игры не трогаются.
 *
 * @property interactor пинг, FPS и их настройки
 */
class GameInfoViewModel(private val interactor: PingInteractor) : ViewModel() {
    private val mutableState = MutableStateFlow(GameInfoState(fpsAvailable = interactor.fpsAvailable))
    private var pingWatcher: Job? = null
    private var fpsWatcher: Job? = null

    /** Состояние вкладки и окна пинга. */
    val state: StateFlow<GameInfoState> = mutableState.asStateFlow()

    /** Может ли платформа мерить пинг (только настольная версия). */
    val supported: Boolean get() = interactor.supported

    init {
        viewModelScope.launch {
            val settings = interactor.settings()
            mutableState.update { it.copy(settings = settings) }
            updateWatching()
            if (settings.gridShown) readUiHeight()
        }
    }

    /** Действие игрока. */
    fun onEvent(event: GameInfoEvent) {
        when (event) {
            GameInfoEvent.TogglePing -> change(save = true) { it.copy(shown = !it.shown) }.also { updateWatching() }
            GameInfoEvent.ToggleFps -> if (interactor.fpsAvailable) {
                change(save = true) { it.copy(fpsShown = !it.fpsShown) }.also { updateWatching() }
            }
            is GameInfoEvent.SetFpsStyle -> change(save = true) { it.copy(fpsStyle = event.style) }
            GameInfoEvent.ToggleTime -> change(save = true) { it.copy(timeShown = !it.timeShown) }
            GameInfoEvent.ToggleGrid -> {
                change(save = true) { it.copy(gridShown = !it.gridShown) }
                // масштаб интерфейса игры читается при каждом включении: его могли поменять
                if (mutableState.value.settings.gridShown) readUiHeight()
            }
            is GameInfoEvent.SetGridStep -> change(save = true) { it.copy(gridStep = event.step) }
            is GameInfoEvent.SetGridOpacity -> change(event.save) { it.copy(gridOpacity = event.percent) }
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

    /** Мерить пинг и FPS, пока их показывают и платформа умеет; иначе остановить замер. */
    private fun updateWatching() {
        val state = mutableState.value
        pingWatcher = pingWatcher.keepIf(state.settings.shown && interactor.supported) {
            interactor.watch().collect { reading -> mutableState.update { it.copy(ping = reading.ms, needsAdmin = reading.needsAdmin) } }
        }
        if (pingWatcher == null) mutableState.update { it.copy(ping = null, needsAdmin = false) }
        fpsWatcher = fpsWatcher.keepIf(state.fpsShown) {
            interactor.watchFps().collect { fps -> mutableState.update { it.copy(fps = fps) } }
        }
        if (fpsWatcher == null) mutableState.update { it.copy(fps = null) }
    }

    /** Читает размер экрана игры для сетки; не прочиталось — подсказка в настройках. */
    private fun readUiHeight() {
        viewModelScope.launch {
            val height = interactor.uiHeight()
            mutableState.update { it.copy(uiHeight = height, gridUnreadable = height == null) }
        }
    }

    /** Замер идёт, пока [watch]: запускает [block], если ещё не идёт, и останавливает, когда больше не нужен. */
    private fun Job?.keepIf(watch: Boolean, block: suspend CoroutineScope.() -> Unit): Job? = when {
        !watch -> {
            this?.cancel()
            null
        }
        this?.isActive == true -> this
        else -> viewModelScope.launch(block = block)
    }
}
