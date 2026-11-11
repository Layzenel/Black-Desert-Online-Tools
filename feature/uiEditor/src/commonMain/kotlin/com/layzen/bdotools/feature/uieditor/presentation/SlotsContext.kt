package com.layzen.bdotools.feature.uieditor.presentation

import com.layzen.bdotools.feature.uieditor.domain.error.SlotsError
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsException
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsNotice
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Сколько шагов помнит «Отменить». */
private const val HISTORY_LIMIT = 50

/** Общее для ViewModel и контроллеров: состояние, запуск задач с обработкой ошибок и правка раскладки. */
internal class SlotsContext(private val scope: CoroutineScope) {

    private val _state = MutableStateFlow(SlotsState())
    /** Состояние экрана; меняют его только контроллеры через [update] и [editLayout]. */
    val state: StateFlow<SlotsState> = _state.asStateFlow()

    /** Текущее значение [state] без подписки, для контроллеров. */
    val current: SlotsState get() = _state.value

    /** Атомарно заменяет состояние результатом [change]. */
    fun update(change: (SlotsState) -> SlotsState) = _state.update(change)

    /**
     * Меняет раскладку и запоминает прежнюю для «Отменить»; отменённое до этого уже не повторить. [preset] — откуда взята новая раскладка;
     * `null` — правка руками.
     */
    fun editLayout(preset: PresetRef? = null, change: (SlotLayout) -> SlotLayout) = update {
        val changed = change(it.layout)
        if (changed == it.layout) return@update it.copy(activePreset = preset ?: it.activePreset)
        it.copy(
            layout = changed,
            history = (it.history + it.layout).takeLast(HISTORY_LIMIT),
            future = emptyList(),
            activePreset = preset,
        )
    }

    /** Запоминает [previous] для «Отменить» после правки, которая шла по шагам (перетаскивание). */
    fun commitEdit(previous: SlotLayout) = update {
        if (it.layout == previous) it
        else it.copy(history = (it.history + previous).takeLast(HISTORY_LIMIT), future = emptyList(), activePreset = null)
    }

    /** Запускает задачу; ошибку файлов показывает сообщением. Отмена выбора файла — не ошибка. */
    fun launch(busy: Boolean = false, action: suspend () -> Unit) {
        scope.launch {
            if (busy) update { it.copy(busy = true) }
            try {
                action()
            } catch (e: SlotsException) {
                if (e.error != SlotsError.Cancelled) update { it.copy(notice = SlotsNotice.Failed(e.error)) }
            } finally {
                if (busy) update { it.copy(busy = false) }
            }
        }
    }
}
