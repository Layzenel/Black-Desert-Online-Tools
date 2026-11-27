package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.labelSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.lineWidth
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorColors
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import kotlin.math.hypot

/**
 * Правка, которую тянут мышью инструментом из [state] от [start] до [current]; у кисти — продолжение
 * [previous] до [current]. Текст ставится кликом, поэтому для него `null`.
 */
internal fun dragDraft(state: ScreenshotEditorState, start: ImagePoint, current: ImagePoint, previous: Edit?): Edit? {
    val image = state.image ?: return null
    val color = EditorColors.of(state.colorIndex)
    val width = lineWidth(state.thickness, image.width, image.height)
    val area = { rect: ImageRect -> rect.snappedInto(image.width, image.height) }
    return when (state.tool) {
        EditorTool.Crop -> Edit.Crop(area(state.aspect.rect(start, current)))
        EditorTool.Pixelate -> Edit.Pixelate(area(ImageRect.between(start, current)))
        EditorTool.Arrow -> Edit.Arrow(start, current, color, width)
        EditorTool.Frame -> Edit.Frame(ImageRect.between(start, current), color, width)
        EditorTool.Brush -> Edit.Stroke(((previous as? Edit.Stroke)?.points ?: listOf(start)) + current, color, width)
        EditorTool.Text -> null
    }
}

/** Надпись из поля «Текст» в точке [at]; пустое поле — ничего. */
internal fun labelAt(state: ScreenshotEditorState, at: ImagePoint): Edit? {
    val image = state.image ?: return null
    if (state.tool != EditorTool.Text || state.text.isBlank()) return null
    return Edit.Label(at, state.text.trim(), EditorColors.of(state.colorIndex), labelSize(state.thickness, image.width, image.height))
}

/** Правку стоит сохранить в историю: случайный клик без движения не даёт пустую рамку или стрелку-точку. */
internal fun Edit.isMeaningful(): Boolean = when (this) {
    is Edit.Crop -> !rect.isEmpty
    is Edit.Pixelate -> !rect.isEmpty
    is Edit.Frame -> !rect.isEmpty
    is Edit.Arrow -> hypot(to.x - from.x, to.y - from.y) >= width
    is Edit.Stroke -> points.isNotEmpty()
    is Edit.Label -> text.isNotBlank()
}
