package com.layzen.bdotools.feature.screenshoteditor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ArrowStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.DEFAULT_HIDE_STRENGTH
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.FrameShape
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.HideMode
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.LabelStyle
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.MAX_HIDE_STRENGTH
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.isDrawn
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedBy
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale

/** Наибольшая толщина линии в настройках. */
internal const val MAX_THICKNESS = 20

/**
 * Состояние редактора скриншотов.
 *
 * @property image открытый скриншот; `null` — ещё ничего не открыли
 * @property name имя файла скриншота: от него строится имя копии
 * @property history правки и их отмена
 * @property saved правки на момент последнего сохранения копии: по ним видно, есть ли несохранённое
 * @property draft правка, которую сейчас тянут мышью: видна, но в историю попадёт, когда отпустят
 * @property pendingCrop обведённая область обрезки, которую ещё не подтвердили галочкой
 * @property selected номер выбранной правки в [EditHistory.edits]: её можно сдвинуть или удалить
 * @property dragOffset насколько сейчас тянут выбранную правку или [pendingCrop], в пикселях картинки
 * @property resized выбранная правка, у которой сейчас тянут ручку: видна такой, в историю попадёт, когда отпустят
 * @property tool инструмент; `null` — не выбран: мышь только выбирает и двигает пометки
 * @property color цвет пометок (ARGB)
 * @property recentColors цвета, взятые пипеткой, — новые первыми, их показывает палитра
 * @property picking следующий клик по скриншоту берёт цвет пипеткой
 * @property thickness толщина линий и размер текста, 1…[MAX_THICKNESS]
 * @property arrowStyle вид новых стрелок
 * @property frameShape фигура новых рамок
 * @property labelStyle вид новых надписей
 * @property hideMode как скрывать новые области
 * @property hideStrength сила скрытия новых областей, 1…[MAX_HIDE_STRENGTH]
 * @property text текст надписи, которую сейчас набирают на холсте
 * @property textAt где на картинке набирают надпись (её левый верхний угол); `null` — поле ввода не открыто
 * @property editing номер надписи, которую правят на холсте (двойной клик); `null` — набирают новую
 * @property copied скопированная пометка для Ctrl+V
 * @property busy идёт открытие или сохранение
 * @property comparing показать исходный скриншот без правок — сравнить «до» и «после»
 */
data class ScreenshotEditorState(
    val image: ImageBitmap? = null,
    val name: String = "",
    val history: EditHistory = EditHistory(),
    val saved: List<Edit> = emptyList(),
    val draft: Edit? = null,
    val pendingCrop: ImageRect? = null,
    val selected: Int? = null,
    val dragOffset: ImagePoint? = null,
    val resized: Edit? = null,
    val tool: EditorTool? = null,
    val color: Long = EditorColors.DEFAULT,
    val recentColors: List<Long> = emptyList(),
    val picking: Boolean = false,
    val thickness: Int = 4,
    val arrowStyle: ArrowStyle = ArrowStyle.Single,
    val frameShape: FrameShape = FrameShape.Rectangle,
    val labelStyle: LabelStyle = LabelStyle.Shadow,
    val hideMode: HideMode = HideMode.Pixelate,
    val hideStrength: Int = DEFAULT_HIDE_STRENGTH,
    val text: String = "",
    val textAt: ImagePoint? = null,
    val editing: Int? = null,
    val copied: Edit? = null,
    val aspect: CropAspect = CropAspect.Free,
    val scale: ExportScale = ExportScale.Full,
    val format: ExportFormat = ExportFormat.Png,
    val busy: Boolean = false,
    val notice: EditorNotice? = null,
    val comparing: Boolean = false,
) {
    /** Ретушь на экране: пока ползунок тянут — из черновика, иначе из истории. */
    val shownAdjustments: Adjustments get() = (draft as? Edit.Adjust)?.adjustments ?: history.adjustments

    /** Есть правки, которых нет в сохранённой копии: закрытие скриншота их потеряет. */
    val dirty: Boolean get() = history.edits != saved

    /** Область обрезки на экране — с учётом того, насколько её сейчас тянут. */
    val shownCrop: ImageRect? get() = pendingCrop?.let { rect -> dragOffset?.let { rect.movedBy(it.x, it.y) } ?: rect }

    /** Выбранная правка на экране — с учётом того, насколько её сейчас тянут. */
    val shownSelection: Edit? get() = selected?.let(::shownEdit)

    /** Правки поверх картинки по порядку, выбранная — на том месте, куда её тянут; надпись, которую правят, — в поле ввода. */
    val shownDrawn: List<Edit> get() = history.edits.indices.filter { it != editing }.map(::shownEdit).filter { it.isDrawn }

    /** Выбранная правка как есть в истории. */
    val selectedEdit: Edit? get() = selected?.let(history.edits::getOrNull)

    private fun shownEdit(index: Int): Edit {
        val edit = history.edits[index]
        if (index == selected) resized?.let { return it }
        val offset = dragOffset?.takeIf { index == selected } ?: return edit
        return edit.movedBy(offset.x, offset.y)
    }
}
