package com.layzen.bdotools.feature.screenshoteditor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale

/** Наибольшая толщина линии в настройках. */
internal const val MAX_THICKNESS = 20

/**
 * Состояние редактора скриншотов.
 *
 * @property image открытый скриншот; `null` — ещё ничего не открыли
 * @property name имя файла скриншота: от него строится имя копии
 * @property history правки и отменённые правки
 * @property draft правка, которую сейчас тянут мышью: видна, но в историю попадёт, когда отпустят
 * @property colorIndex цвет пометок — номер в [EditorColors.choices], `null` — цвет по умолчанию
 * @property thickness толщина линий и размер текста, 1…[MAX_THICKNESS]
 * @property text текст для инструмента «Текст»
 * @property busy идёт открытие или сохранение
 */
data class ScreenshotEditorState(
    val image: ImageBitmap? = null,
    val name: String = "",
    val history: EditHistory = EditHistory(),
    val draft: Edit? = null,
    val tool: EditorTool = EditorTool.Crop,
    val colorIndex: Int? = null,
    val thickness: Int = 4,
    val text: String = "",
    val aspect: CropAspect = CropAspect.Free,
    val scale: ExportScale = ExportScale.Full,
    val format: ExportFormat = ExportFormat.Png,
    val busy: Boolean = false,
    val notice: EditorNotice? = null,
)
