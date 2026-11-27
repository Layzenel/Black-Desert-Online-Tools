package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments

/**
 * Правки скриншота по порядку и прошлые списки правок для отмены и повтора. Любое изменение — добавить, сдвинуть,
 * удалить, сбросить — отменяется одним шагом. Новое изменение очищает повтор, как в любом редакторе.
 *
 * @property edits применённые правки, последняя — в конце
 * @property past списки правок до каждого изменения, последний — в конце
 * @property future отменённые списки правок для повтора, последний отменённый — в конце
 */
data class EditHistory(
    val edits: List<Edit> = emptyList(),
    val past: List<List<Edit>> = emptyList(),
    val future: List<List<Edit>> = emptyList(),
) {
    /** Есть что отменить. */
    val canUndo: Boolean get() = past.isNotEmpty()

    /** Есть что повторить. */
    val canRedo: Boolean get() = future.isNotEmpty()

    /** Действующая обрезка — последняя в истории; `null` — картинка целиком. */
    val crop: ImageRect? get() = edits.filterIsInstance<Edit.Crop>().lastOrNull()?.rect

    /** Действующая ретушь — последняя в истории. */
    val adjustments: Adjustments get() = edits.filterIsInstance<Edit.Adjust>().lastOrNull()?.adjustments ?: Adjustments()

    /** Действующий поворот — последний в истории. */
    val orientation: Orientation get() = edits.filterIsInstance<Edit.Orient>().lastOrNull()?.orientation ?: Orientation()

    /** Правки, которые рисуются на картинке поверх: всё, кроме обрезки, ретуши и поворота. */
    val drawn: List<Edit> get() = edits.filter { it.isDrawn }

    /** История с новой правкой [edit]. */
    fun add(edit: Edit) = changed(edits + edit)

    /** История, где правка номер [index] заменена на [edit] — её сдвинули. */
    fun replace(index: Int, edit: Edit) = changed(edits.toMutableList().also { it[index] = edit })

    /** История без правки номер [index]. */
    fun remove(index: Int) = changed(edits.filterIndexed { i, _ -> i != index })

    /** История без всех правок: «Сбросить изменения» тоже отменяется. */
    fun cleared(): EditHistory = if (edits.isEmpty()) this else changed(emptyList())

    /** Прошлый список правок; текущий можно вернуть [redo]. */
    fun undo(): EditHistory = if (!canUndo) this else EditHistory(past.last(), past.dropLast(1), future + listOf(edits))

    /** Последний отменённый список правок обратно. */
    fun redo(): EditHistory = if (!canRedo) this else EditHistory(future.last(), past + listOf(edits), future.dropLast(1))

    private fun changed(next: List<Edit>) = EditHistory(next, past + listOf(edits))
}

/** Правка рисуется поверх картинки и её можно выбрать, сдвинуть и удалить; обрезка, ретушь и поворот — нет. */
val Edit.isDrawn: Boolean get() = this !is Edit.Crop && this !is Edit.Adjust && this !is Edit.Orient
