package com.layzen.bdotools.feature.screenshoteditor.presentation.event

import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.FilterPreset
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.HideMode
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.MarkStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Resize
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool

/**
 * Действия на экране редактора. Сохранению и копированию нужен [TextMeasurer] экрана: надписи рисуются
 * шрифтом приложения, а шрифты знает только Compose.
 */
sealed interface ScreenshotEditorEvent {
    data object Open : ScreenshotEditorEvent
    data class OpenEntry(val entry: ScreenshotEntry) : ScreenshotEditorEvent
    data object Close : ScreenshotEditorEvent
    data class Save(val measurer: TextMeasurer) : ScreenshotEditorEvent
    data class Copy(val measurer: TextMeasurer) : ScreenshotEditorEvent
    data object Undo : ScreenshotEditorEvent
    data object Redo : ScreenshotEditorEvent
    data object Discard : ScreenshotEditorEvent
    data class SelectTool(val tool: EditorTool) : ScreenshotEditorEvent
    /** Цвет пометок (ARGB) — из палитры или взятый пипеткой. */
    data class SelectColor(val color: Long) : ScreenshotEditorEvent
    data class Thickness(val value: Int) : ScreenshotEditorEvent

    /** Вид стрелки, фигура рамки или вид надписи: выбирает и инструмент, и вид выбранной пометки того же рода. */
    data class SelectStyle(val style: MarkStyle) : ScreenshotEditorEvent
    data class Hide(val mode: HideMode) : ScreenshotEditorEvent
    data class HideStrength(val value: Int) : ScreenshotEditorEvent

    /** Следующий клик по скриншоту берёт цвет пипеткой. */
    data object StartPicking : ScreenshotEditorEvent

    /** Правая кнопка над правкой номер [hit] (`null` — мимо): выбрать её перед меню. */
    data class ContextPress(val hit: Int?) : ScreenshotEditorEvent

    /** Двойной клик по надписи номер [index]: править её текст на холсте. */
    data class EditLabel(val index: Int) : ScreenshotEditorEvent
    data object CopyMark : ScreenshotEditorEvent
    data object PasteMark : ScreenshotEditorEvent
    data object DuplicateMark : ScreenshotEditorEvent
    data class Text(val value: String) : ScreenshotEditorEvent
    data class Aspect(val aspect: CropAspect) : ScreenshotEditorEvent
    data class Scale(val scale: ExportScale) : ScreenshotEditorEvent
    data class Format(val format: ExportFormat) : ScreenshotEditorEvent
    /**
     * Нажали левую кнопку в точке [at] над правкой номер [hit] в истории (`null` — не над правкой) или над ручкой
     * выбранной правки ([resize]).
     */
    data class Press(val at: ImagePoint, val hit: Int?, val resize: Resize? = null) : ScreenshotEditorEvent
    data class DragMove(val to: ImagePoint) : ScreenshotEditorEvent
    data object DragEnd : ScreenshotEditorEvent
    data class Tap(val at: ImagePoint) : ScreenshotEditorEvent

    /** Правая кнопка или Esc: отменить обведённую обрезку и снять выбор. */
    data object Cancel : ScreenshotEditorEvent
    data object ConfirmCrop : ScreenshotEditorEvent
    data object DeleteSelected : ScreenshotEditorEvent

    /** Enter в поле надписи на холсте: надпись — в историю. */
    data object CommitText : ScreenshotEditorEvent

    /** Shift+колесо над выбранной правкой: увеличить её в [factor] раз от точки [around]. */
    data class ScaleSelected(val factor: Float, val around: ImagePoint) : ScreenshotEditorEvent
    data class ApplyToPortraits(val measurer: TextMeasurer) : ScreenshotEditorEvent

    /** Ползунок ретуши двигают: видно сразу, в историю попадёт по [AdjustFinished]. */
    data class Adjust(val adjustments: Adjustments) : ScreenshotEditorEvent
    data object AdjustFinished : ScreenshotEditorEvent
    data class Preset(val preset: FilterPreset) : ScreenshotEditorEvent
    data object Rotate : ScreenshotEditorEvent
    data object Flip : ScreenshotEditorEvent
    data object ToggleCompare : ScreenshotEditorEvent
    data object DismissNotice : ScreenshotEditorEvent
}
