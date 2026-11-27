package com.layzen.bdotools.feature.screenshoteditor.presentation.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotInteractor
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.encode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.ScreenshotWatchInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Уведомление висит 8 с после последнего скриншота: дольше оно мешает игре, а вернуться к снимку можно из галереи
private const val ALERT_MS = 8_000L

/**
 * Следит за новыми скриншотами, пока предложение включено, и держит уведомление поверх игры. Одна на приложение:
 * слежение идёт, даже когда окно программы свёрнуто в трей.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotAlertViewModel(
    private val watch: ScreenshotWatchInteractor,
    private val screenshots: ScreenshotInteractor,
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenshotAlertState(enabled = watch.alertsEnabled))

    /** Состояние уведомления. */
    val state: StateFlow<ScreenshotAlertState> = _state.asStateFlow()

    /** Слежение есть только в настольной версии. */
    val supported: Boolean get() = watch.supported

    private var hide: Job? = null

    init {
        if (watch.supported) {
            viewModelScope.launch {
                _state.distinctUntilChangedBy { it.enabled }
                    .flatMapLatest { if (it.enabled) watch.newScreenshots() else emptyFlow() }
                    .collect(::show)
            }
        }
    }

    /** Включает или выключает предложение редактировать; выключенное сразу убирает уведомление. */
    fun setEnabled(enabled: Boolean) {
        watch.alertsEnabled = enabled
        hide?.cancel()
        _state.update { if (enabled) it.copy(enabled = true) else ScreenshotAlertState(enabled = false) }
    }

    /** Убирает уведомление (по нему кликнули или вышло время). */
    fun dismiss() {
        hide?.cancel()
        _state.update { it.copy(latest = null, count = 0) }
    }

    /**
     * Копирует скриншот [entry] в буфер обмена PNG-картинкой и убирает уведомление. Не вышло — уведомление остаётся:
     * можно открыть скриншот в редакторе.
     */
    fun copy(entry: ScreenshotEntry) {
        viewModelScope.launch {
            try {
                val shot = screenshots.open(entry)
                val png = withContext(Dispatchers.Default) {
                    runCatching { encode(shot.bytes.decodeToImageBitmap(), ExportFormat.Png) }.getOrNull()
                } ?: return@launch
                screenshots.copy(png)
                dismiss()
            } catch (_: ScreenshotException) {
                // shortcut: ошибку поверх игры не показываем, уведомление просто остаётся; показать — если будут жалобы
            }
        }
    }

    private fun show(entry: ScreenshotEntry) {
        _state.update { it.copy(latest = entry, count = if (it.latest == null) 1 else it.count + 1) }
        hide?.cancel()
        hide = viewModelScope.launch {
            delay(ALERT_MS)
            _state.update { it.copy(latest = null, count = 0) }
        }
    }
}
