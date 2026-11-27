package com.layzen.bdotools.feature.screenshoteditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotInteractor
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.dragDraft
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.isMeaningful
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.labelAt
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.encode
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.renderEdited
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
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

/** Редактор скриншотов: открытие, правки мышью, отмена и выдача результата через [ScreenshotInteractor]. */
class ScreenshotEditorViewModel(private val interactor: ScreenshotInteractor) : ViewModel() {
    private val _state = MutableStateFlow(ScreenshotEditorState())

    /** Состояние экрана. */
    val state: StateFlow<ScreenshotEditorState> = _state.asStateFlow()

    private var dragStart: ImagePoint? = null

    /** Обрабатывает действие на экране. */
    fun onEvent(event: ScreenshotEditorEvent) {
        when (event) {
            ScreenshotEditorEvent.Open -> open()
            is ScreenshotEditorEvent.Save -> save(event.measurer)
            is ScreenshotEditorEvent.Copy -> copy(event.measurer)
            ScreenshotEditorEvent.Undo -> _state.update { it.copy(history = it.history.undo()) }
            ScreenshotEditorEvent.Redo -> _state.update { it.copy(history = it.history.redo()) }
            ScreenshotEditorEvent.ResetCrop -> resetCrop()
            is ScreenshotEditorEvent.SelectTool -> _state.update { it.copy(tool = event.tool) }
            is ScreenshotEditorEvent.SelectColor -> _state.update { it.copy(colorIndex = event.index) }
            is ScreenshotEditorEvent.Thickness -> _state.update { it.copy(thickness = event.value.coerceIn(1, MAX_THICKNESS)) }
            is ScreenshotEditorEvent.Text -> _state.update { it.copy(text = event.value) }
            is ScreenshotEditorEvent.Aspect -> _state.update { it.copy(aspect = event.aspect) }
            is ScreenshotEditorEvent.Scale -> _state.update { it.copy(scale = event.scale) }
            is ScreenshotEditorEvent.Format -> _state.update { it.copy(format = event.format) }
            is ScreenshotEditorEvent.DragStart -> dragStart = event.at.also { at -> _state.update { it.copy(draft = dragDraft(it, at, at, null)) } }
            is ScreenshotEditorEvent.DragMove -> dragTo(event.to)
            ScreenshotEditorEvent.DragEnd -> finishDrag()
            is ScreenshotEditorEvent.Tap -> _state.update { state -> labelAt(state, event.at)?.let { state.copy(history = state.history.add(it)) } ?: state }
            ScreenshotEditorEvent.DismissNotice -> _state.update { it.copy(notice = null) }
        }
    }

    private fun dragTo(to: ImagePoint) {
        val start = dragStart ?: return
        _state.update { it.copy(draft = dragDraft(it, start, to, it.draft)) }
    }

    private fun finishDrag() {
        dragStart = null
        _state.update { state ->
            val draft = state.draft?.takeIf(Edit::isMeaningful)
            state.copy(draft = null, history = draft?.let(state.history::add) ?: state.history)
        }
    }

    /** Обрезка сбрасывается новой правкой «вся картинка» — так сброс тоже отменяется. */
    private fun resetCrop() = _state.update { state ->
        val image = state.image ?: return@update state
        if (state.history.crop == null) return@update state
        val whole = ImageRect(0f, 0f, image.width.toFloat(), image.height.toFloat())
        state.copy(history = state.history.add(Edit.Crop(whole)))
    }

    private fun open() = viewModelScope.launch {
        val shot = runAction { interactor.open() } ?: return@launch
        _state.update { it.copy(busy = true) }
        delay(FRAME_MS)
        val image = withContext(Dispatchers.Default) { runCatching { shot.bytes.decodeToImageBitmap() }.getOrNull() }
        _state.update {
            if (image == null) it.copy(busy = false, notice = EditorNotice.Failed(ScreenshotError.NotImage))
            else it.copy(busy = false, image = image, name = shot.name, history = EditHistory(), draft = null)
        }
    }

    private fun save(measurer: TextMeasurer) = export(measurer, _state.value.format) { bytes ->
        val format = _state.value.format
        interactor.saveCopy(_state.value.name, format, bytes)?.let(EditorNotice::Saved) ?: EditorNotice.Downloaded
    }

    // браузер кладёт в буфер только PNG
    private fun copy(measurer: TextMeasurer) = export(measurer, ExportFormat.Png) { png ->
        interactor.copy(png)
        EditorNotice.Copied
    }

    /** Рисует картинку с правками в [format] и отдаёт байты [deliver]; ошибки показывает сообщением. */
    private fun export(measurer: TextMeasurer, format: ExportFormat, deliver: suspend (ByteArray) -> EditorNotice) =
        viewModelScope.launch {
            val state = _state.value
            val image = state.image ?: return@launch
            _state.update { it.copy(busy = true) }
            delay(FRAME_MS)
            val notice = try {
                val bytes = withContext(Dispatchers.Default) { encode(renderEdited(image, state.history, state.scale, measurer), format) }
                deliver(bytes)
            } catch (e: ScreenshotException) {
                EditorNotice.Failed(e.error)
            }
            _state.update { it.copy(busy = false, notice = notice) }
        }

    private suspend fun <T> runAction(action: suspend () -> T): T? =
        try {
            action()
        } catch (e: ScreenshotException) {
            _state.update { it.copy(notice = EditorNotice.Failed(e.error)) }
            null
        }
}
