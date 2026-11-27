package com.layzen.bdotools.feature.screenshoteditor.presentation

import androidx.compose.ui.text.TextMeasurer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import com.layzen.bdotools.feature.screenshoteditor.domain.file.Screenshot
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotInteractor
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.CanvasGesture
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.reduce
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.encode
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.renderEdited
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

// В браузере всё в одном потоке: пауза перед тяжёлым шагом даёт показать окно ожидания
private const val FRAME_MS = 16L

/**
 * Редактор скриншотов: открытие, правки мышью, отмена и выдача результата через [ScreenshotInteractor].
 * Одна на приложение: окно уведомления поверх игры открывает в ней новый скриншот.
 */
class ScreenshotEditorViewModel(private val interactor: ScreenshotInteractor) : ViewModel() {
    private val _state = MutableStateFlow(ScreenshotEditorState())

    /** Состояние экрана. */
    val state: StateFlow<ScreenshotEditorState> = _state.asStateFlow()

    private val gesture = CanvasGesture()

    /** Обрабатывает действие на экране. */
    fun onEvent(event: ScreenshotEditorEvent) {
        when (event) {
            ScreenshotEditorEvent.Open -> load { interactor.open() }
            is ScreenshotEditorEvent.OpenEntry -> load { interactor.open(event.entry) }
            is ScreenshotEditorEvent.Save -> save(event.measurer)
            is ScreenshotEditorEvent.Copy -> copy(event.measurer)
            is ScreenshotEditorEvent.ApplyToPortraits -> applyToPortraits(event.measurer)
            is ScreenshotEditorEvent.Press -> _state.update { gesture.press(it, event.at, event.hit) }
            is ScreenshotEditorEvent.DragMove -> _state.update { gesture.move(it, event.to) }
            ScreenshotEditorEvent.DragEnd -> _state.update(gesture::release)
            is ScreenshotEditorEvent.Tap -> _state.update { gesture.tap(it, event.at) }
            else -> _state.update { it.reduce(event) ?: it }
        }
    }

    /** Открывает скриншот из [source] (`null` — окно выбора закрыли); правки прошлого скриншота сбрасываются. */
    private fun load(source: suspend () -> Screenshot?) = viewModelScope.launch {
        _state.update { it.copy(busy = true) }
        val shot = try {
            source()
        } catch (e: ScreenshotException) {
            _state.update { it.copy(busy = false, notice = EditorNotice.Failed(e.error)) }
            return@launch
        }
        if (shot == null) {
            _state.update { it.copy(busy = false) }
            return@launch
        }
        delay(FRAME_MS)
        val image = withContext(Dispatchers.Default) { runCatching { shot.bytes.decodeToImageBitmap() }.getOrNull() }
        _state.update {
            if (image == null) it.copy(busy = false, notice = EditorNotice.Failed(ScreenshotError.NotImage))
            else it.copy(busy = false, image = image, name = shot.name, history = EditHistory(), saved = emptyList(), draft = null, pendingCrop = null, selected = null, comparing = false)
        }
    }

    private fun save(measurer: TextMeasurer) {
        val edits = _state.value.history.edits
        export(measurer, _state.value.format, _state.value.scale) { bytes ->
            val notice = interactor.saveCopy(_state.value.name, _state.value.format, bytes)?.let(EditorNotice::Saved) ?: EditorNotice.Downloaded
            _state.update { it.copy(saved = edits) }
            notice
        }
    }

    // браузер кладёт в буфер только PNG
    private fun copy(measurer: TextMeasurer) = export(measurer, ExportFormat.Png, _state.value.scale) { png ->
        interactor.copy(png)
        EditorNotice.Copied
    }

    // портреты нарезаются из полного размера: уменьшенная копия дала бы мыло
    private fun applyToPortraits(measurer: TextMeasurer) = export(measurer, ExportFormat.Png, ExportScale.Full) { png ->
        interactor.sendToPortraits(png)
        null
    }

    /**
     * Рисует картинку с правками в [format] и размере [scale] и отдаёт байты [deliver]; его сообщение или ошибку
     * показывает внизу экрана.
     */
    private fun export(measurer: TextMeasurer, format: ExportFormat, scale: ExportScale, deliver: suspend (ByteArray) -> EditorNotice?) =
        viewModelScope.launch {
            val state = _state.value
            val image = state.image ?: return@launch
            _state.update { it.copy(busy = true) }
            delay(FRAME_MS)
            val notice = try {
                val bytes = withContext(Dispatchers.Default) { encode(renderEdited(image, state.history, scale, measurer), format) }
                deliver(bytes)
            } catch (e: ScreenshotException) {
                EditorNotice.Failed(e.error)
            }
            _state.update { it.copy(busy = false, notice = notice) }
        }
}
