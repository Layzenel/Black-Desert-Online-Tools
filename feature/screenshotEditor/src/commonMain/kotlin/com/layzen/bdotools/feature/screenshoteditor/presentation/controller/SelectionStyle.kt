package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.counterRadius
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.labelSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.lineWidth
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.recolored
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.snappedInto
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorColors
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import kotlin.math.roundToInt

// Непрозрачность цвета, взятого пипеткой: пометки всегда непрозрачные
private const val OPAQUE = 0xFF000000L
private const val ARGB_MASK = 0xFFFFFFFFL

/**
 * Настройки в меню слева — как у выбранной пометки: палитра, ползунок и виды показывают её и меняют её же,
 * а новые пометки рисуются тем же, как в графических редакторах.
 */
internal fun ScreenshotEditorState.withSelectionStyle(): ScreenshotEditorState {
    val edit = selectedEdit ?: return this
    val thickness = thicknessOf(edit) ?: thickness
    return when (edit) {
        is Edit.Arrow -> copy(color = edit.color, thickness = thickness, arrowStyle = edit.style)
        is Edit.Frame -> copy(color = edit.color, thickness = thickness, frameShape = edit.shape)
        is Edit.Label -> copy(color = edit.color, thickness = thickness, labelStyle = edit.style)
        is Edit.Stroke -> copy(color = edit.color, thickness = thickness)
        is Edit.Counter -> copy(color = edit.color, thickness = thickness)
        is Edit.Pixelate -> copy(hideMode = edit.mode, hideStrength = edit.strength)
        is Edit.Crop, is Edit.Adjust, is Edit.Orient -> this
    }
}

/** Выбранная правка, изменённая [transform]: цвет, толщина или размер; смены одной правки подряд — один шаг отмены. */
internal fun ScreenshotEditorState.restyled(transform: (Edit) -> Edit): ScreenshotEditorState {
    val index = selected ?: return this
    val image = image ?: return this
    val edit = history.edits.getOrNull(index) ?: return this
    val next = transform(edit).snappedInto(image.width, image.height)
    return if (next == edit) this else copy(history = history.restyle(index, next))
}

/** Цвет [color] — для новых пометок и выбранной; пипетка выключается. */
internal fun ScreenshotEditorState.withColor(color: Long): ScreenshotEditorState =
    copy(color = color, picking = false).restyled { it.recolored(color) }

/** Цвет картинки в точке [at], взятый пипеткой: он и в палитре среди последних, если его там ещё нет. */
internal fun ScreenshotEditorState.withPickedColor(at: ImagePoint): ScreenshotEditorState {
    val picked = image?.colorAt(at) ?: return copy(picking = false)
    val known = EditorColors.choices + EditorColors.DEFAULT
    val recent = if (picked in known) recentColors else (listOf(picked) + recentColors).distinct().take(EditorColors.MAX_RECENT)
    return withColor(picked).copy(recentColors = recent)
}

/** Пометка толщины [thickness] из настроек: у линий — толщина линии, у надписи — размер букв, у номера — радиус. */
internal fun ScreenshotEditorState.withThickness(edit: Edit, thickness: Int): Edit {
    val image = image ?: return edit
    val width = lineWidth(thickness, image.width, image.height)
    return when (edit) {
        is Edit.Arrow -> edit.copy(width = width)
        is Edit.Frame -> edit.copy(width = width)
        is Edit.Stroke -> edit.copy(width = width)
        is Edit.Label -> edit.copy(size = labelSize(thickness, image.width, image.height))
        is Edit.Counter -> edit.copy(radius = counterRadius(thickness, image.width, image.height))
        is Edit.Crop, is Edit.Pixelate, is Edit.Adjust, is Edit.Orient -> edit
    }
}

/** Толщина пометки [edit] в шагах настроек; `null` — у правки нет толщины. */
private fun ScreenshotEditorState.thicknessOf(edit: Edit): Int? {
    val image = image ?: return null
    val (value, unit) = when (edit) {
        is Edit.Arrow -> edit.width to lineWidth(1, image.width, image.height)
        is Edit.Frame -> edit.width to lineWidth(1, image.width, image.height)
        is Edit.Stroke -> edit.width to lineWidth(1, image.width, image.height)
        is Edit.Label -> edit.size to labelSize(1, image.width, image.height)
        is Edit.Counter -> edit.radius to counterRadius(1, image.width, image.height)
        is Edit.Crop, is Edit.Pixelate, is Edit.Adjust, is Edit.Orient -> return null
    }
    return (value / unit).roundToInt().coerceIn(1, MAX_THICKNESS)
}

/** Цвет пикселя (ARGB, непрозрачный) в точке [at]; `null` — точка за краем картинки. */
private fun ImageBitmap.colorAt(at: ImagePoint): Long? {
    val x = at.x.toInt()
    val y = at.y.toInt()
    if (x !in 0 until width || y !in 0 until height) return null
    val pixel = IntArray(1)
    readPixels(pixel, x, y, 1, 1)
    return (pixel[0].toLong() and ARGB_MASK) or OPAQUE
}
