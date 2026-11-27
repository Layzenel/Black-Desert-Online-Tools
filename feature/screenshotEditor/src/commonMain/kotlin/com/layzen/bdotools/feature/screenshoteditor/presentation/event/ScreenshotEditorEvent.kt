package com.layzen.bdotools.feature.screenshoteditor.presentation.event

import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool

/**
 * Действия на экране редактора. Сохранению и копированию нужен [TextMeasurer] экрана: надписи рисуются
 * шрифтом приложения, а шрифты знает только Compose.
 */
sealed interface ScreenshotEditorEvent {
    data object Open : ScreenshotEditorEvent
    data class Save(val measurer: TextMeasurer) : ScreenshotEditorEvent
    data class Copy(val measurer: TextMeasurer) : ScreenshotEditorEvent
    data object Undo : ScreenshotEditorEvent
    data object Redo : ScreenshotEditorEvent
    data object ResetCrop : ScreenshotEditorEvent
    data class SelectTool(val tool: EditorTool) : ScreenshotEditorEvent
    data class SelectColor(val index: Int?) : ScreenshotEditorEvent
    data class Thickness(val value: Int) : ScreenshotEditorEvent
    data class Text(val value: String) : ScreenshotEditorEvent
    data class Aspect(val aspect: CropAspect) : ScreenshotEditorEvent
    data class Scale(val scale: ExportScale) : ScreenshotEditorEvent
    data class Format(val format: ExportFormat) : ScreenshotEditorEvent
    data class DragStart(val at: ImagePoint) : ScreenshotEditorEvent
    data class DragMove(val to: ImagePoint) : ScreenshotEditorEvent
    data object DragEnd : ScreenshotEditorEvent
    data class Tap(val at: ImagePoint) : ScreenshotEditorEvent
    data object DismissNotice : ScreenshotEditorEvent
}
