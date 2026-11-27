package com.layzen.bdotools.feature.screenshoteditor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
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
 * @property colorIndex цвет пометок — номер в [EditorColors.choices], `null` — цвет по умолчанию
 * @property thickness толщина линий и размер текста, 1…[MAX_THICKNESS]
 * @property text текст для инструмента «Текст»
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
    val tool: EditorTool = EditorTool.Crop,
    val colorIndex: Int? = null,
    val thickness: Int = 4,
    val text: String = "",
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

    /** Правки поверх картинки по порядку, выбранная — на том месте, куда её тянут. */
    val shownDrawn: List<Edit> get() = history.edits.indices.map(::shownEdit).filter { it.isDrawn }

    private fun shownEdit(index: Int): Edit {
        val edit = history.edits[index]
        val offset = dragOffset?.takeIf { index == selected } ?: return edit
        return edit.movedBy(offset.x, offset.y)
    }
}
