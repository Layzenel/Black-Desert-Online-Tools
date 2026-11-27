package com.layzen.bdotools.feature.screenshoteditor.domain.edit

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
    val crop: ImageRect? get() = edits.lastOrNull { it is Edit.Crop }?.let { (it as Edit.Crop).rect }

    /** Правки, которые рисуются на картинке: всё, кроме обрезки. */
    val drawn: List<Edit> get() = edits.filterNot { it is Edit.Crop }

    /** История с новой правкой [edit]. */
    fun add(edit: Edit) = EditHistory(edits + edit)

    /** История без последней правки; её можно вернуть [redo]. */
    fun undo(): EditHistory = if (!canUndo) this else EditHistory(edits.dropLast(1), undone + edits.last())

    /** История с последней отменённой правкой обратно. */
    fun redo(): EditHistory = if (!canRedo) this else EditHistory(edits + undone.last(), undone.dropLast(1))
}
