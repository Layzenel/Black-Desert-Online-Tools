package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.counterRadius
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.labelSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.lineWidth
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import kotlin.math.hypot

/**
 * Правка, которую тянут мышью инструментом из [state] от [start] до [current]; у кисти — продолжение
 * [previous] до [current]. Текст и номер ставятся кликом, а без инструмента рисовать нечем — для них `null`.
 */
internal fun dragDraft(state: ScreenshotEditorState, start: ImagePoint, current: ImagePoint, previous: Edit?): Edit? {
    val image = state.image ?: return null
    val color = state.color
    val width = lineWidth(state.thickness, image.width, image.height)
    val area = { rect: ImageRect -> rect.snappedInto(image.width, image.height) }
    return when (state.tool) {
        EditorTool.Crop -> Edit.Crop(area(state.aspect.rect(start, current, state.history.orientation.sideways)))
        EditorTool.Pixelate -> Edit.Pixelate(area(ImageRect.between(start, current)), state.hideMode, state.hideStrength)
        EditorTool.Arrow -> Edit.Arrow(start, current, color, width, state.arrowStyle)
        EditorTool.Frame -> Edit.Frame(ImageRect.between(start, current), color, width, state.frameShape)
        EditorTool.Brush -> Edit.Stroke(((previous as? Edit.Stroke)?.points ?: listOf(start)) + current, color, width)
        EditorTool.Text, EditorTool.Counter, null -> null
    }
}

/** Кружок со следующим номером в точке [at]: номера идут по порядку, после самого большого на скриншоте. */
internal fun ScreenshotEditorState.counterAt(at: ImagePoint): Edit.Counter? {
    val image = image ?: return null
    val next = (history.edits.filterIsInstance<Edit.Counter>().maxOfOrNull { it.number } ?: 0) + 1
    return Edit.Counter(at, next, color, counterRadius(thickness, image.width, image.height))
}

/**
 * Надпись, которую набирают на холсте, — в истории и выбрана, поле ввода закрыто. Правят старую ([ScreenshotEditorState.editing]) —
 * у неё меняются текст, цвет и вид, а стёртая целиком удаляется; пустая новая просто закрывает поле.
 */
internal fun ScreenshotEditorState.withTypedLabel(): ScreenshotEditorState {
    val at = textAt ?: return this
    val image = image ?: return this
    val closed = copy(textAt = null, text = "", editing = null)
    val index = editing
    val old = index?.let { history.edits.getOrNull(it) as? Edit.Label }
    val typed = text.trim()
    return when {
        index != null && old != null && typed.isEmpty() -> closed.copy(history = history.remove(index), selected = null)
        index != null && old != null -> closed.copy(history = history.replace(index, old.copy(text = typed, color = color, style = labelStyle)), selected = index)
        typed.isEmpty() -> closed
        else -> {
            val label = Edit.Label(at, typed, color, labelSize(thickness, image.width, image.height), labelStyle)
            closed.copy(history = history.add(label), selected = history.edits.size)
        }
    }
}

/** Правку стоит сохранить в историю: случайный клик без движения не даёт пустую рамку или стрелку-точку. */
internal fun Edit.isMeaningful(): Boolean = when (this) {
    is Edit.Crop -> !rect.isEmpty
    is Edit.Pixelate -> !rect.isEmpty
    is Edit.Frame -> !rect.isEmpty
    is Edit.Arrow -> hypot(to.x - from.x, to.y - from.y) >= width
    is Edit.Stroke -> points.isNotEmpty()
    is Edit.Label -> text.isNotBlank()
    is Edit.Counter, is Edit.Adjust, is Edit.Orient -> true
}
