package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments

/**
 * Правки скриншота по порядку и отменённые правки для повтора. Новая правка очищает повтор, как в любом редакторе.
 *
 * @property edits применённые правки, последняя — в конце
 * @property undone отменённые правки, последняя отменённая — в конце
 */
data class EditHistory(val edits: List<Edit> = emptyList(), val undone: List<Edit> = emptyList()) {
    /** Есть что отменить. */
    val canUndo: Boolean get() = edits.isNotEmpty()

    /** Есть что повторить. */
    val canRedo: Boolean get() = undone.isNotEmpty()

    /** Действующая обрезка — последняя в истории; `null` — картинка целиком. */
    val crop: ImageRect? get() = edits.filterIsInstance<Edit.Crop>().lastOrNull()?.rect

    /** Действующая ретушь — последняя в истории. */
    val adjustments: Adjustments get() = edits.filterIsInstance<Edit.Adjust>().lastOrNull()?.adjustments ?: Adjustments()

    /** Действующий поворот — последний в истории. */
    val orientation: Orientation get() = edits.filterIsInstance<Edit.Orient>().lastOrNull()?.orientation ?: Orientation()

    /** Правки, которые рисуются на картинке поверх: всё, кроме обрезки, ретуши и поворота. */
    val drawn: List<Edit> get() = edits.filterNot { it is Edit.Crop || it is Edit.Adjust || it is Edit.Orient }

    /** История с новой правкой [edit]. */
    fun add(edit: Edit) = EditHistory(edits + edit)

    /** История без последней правки; её можно вернуть [redo]. */
    fun undo(): EditHistory = if (!canUndo) this else EditHistory(edits.dropLast(1), undone + edits.last())

    /** История с последней отменённой правкой обратно. */
    fun redo(): EditHistory = if (!canRedo) this else EditHistory(edits + undone.last(), undone.dropLast(1))
}
