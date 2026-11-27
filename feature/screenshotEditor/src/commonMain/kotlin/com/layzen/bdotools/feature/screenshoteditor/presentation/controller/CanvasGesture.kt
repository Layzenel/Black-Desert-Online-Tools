package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedBy
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedWithin
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.within
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/**
 * Одно нажатие мыши на холсте — от нажатия до отпускания. Что оно делает, решается при нажатии:
 * - есть обведённая обрезка: нажатие в ней тянет её, мимо — отменяет её;
 * - над правкой: выбирает её и тянет;
 * - иначе: рисует выбранным инструментом и снимает выбор.
 *
 * Пока тянут, правка видна на новом месте, а в историю попадает одним шагом, когда отпустят.
 */
internal class CanvasGesture {
    private enum class Mode { Draw, MoveSelected, MoveCrop, CropCancelled }

    private var mode: Mode? = null
    private var start: ImagePoint? = null

    /** Нажали в [at] над правкой номер [hit] (`null` — не над правкой). */
    fun press(state: ScreenshotEditorState, at: ImagePoint, hit: Int?): ScreenshotEditorState {
        start = at
        val crop = state.pendingCrop
        val (next, result) = when {
            crop != null && at in crop -> Mode.MoveCrop to state
            crop != null -> Mode.CropCancelled to state.copy(pendingCrop = null)
            hit != null -> Mode.MoveSelected to state.copy(selected = hit)
            else -> Mode.Draw to state.copy(selected = null, draft = dragDraft(state, at, at, null))
        }
        mode = next
        return result
    }

    /** Мышь с зажатой кнопкой в [to]. */
    fun move(state: ScreenshotEditorState, to: ImagePoint): ScreenshotEditorState {
        val from = start ?: return state
        val delta = ImagePoint(to.x - from.x, to.y - from.y)
        return when (mode) {
            Mode.Draw -> state.copy(draft = dragDraft(state, from, to, state.draft))
            Mode.MoveSelected -> state.copy(dragOffset = delta)
            Mode.MoveCrop -> state.copy(dragOffset = state.pendingCrop?.let { cropOffset(it, delta, state.bounds) })
            Mode.CropCancelled, null -> state
        }
    }

    /** Кнопку отпустили: правка из черновика или сдвиг — в историю, обведённая обрезка ждёт галочки. */
    fun release(state: ScreenshotEditorState): ScreenshotEditorState = when (mode) {
        Mode.Draw -> finishDraw(state)
        Mode.MoveSelected -> finishMove(state)
        Mode.MoveCrop -> state.copy(pendingCrop = state.shownCrop, dragOffset = null)
        Mode.CropCancelled, null -> state
    }

    /** Отпустили, не сдвинув мышь: клик по обведённой обрезке отменяет её, клик инструментом «Текст» ставит надпись. */
    fun tap(state: ScreenshotEditorState, at: ImagePoint): ScreenshotEditorState {
        val tapped = mode
        mode = null
        start = null
        return when (tapped) {
            Mode.MoveCrop -> state.copy(pendingCrop = null)
            Mode.Draw -> labelAt(state, at)?.let { state.copy(history = state.history.add(it)) } ?: state
            else -> state
        }
    }

    private fun finishDraw(state: ScreenshotEditorState): ScreenshotEditorState {
        val draft = state.draft?.takeIf(Edit::isMeaningful) ?: return state.copy(draft = null)
        if (draft is Edit.Crop) {
            val rect = draft.rect.within(state.bounds)
            return state.copy(draft = null, pendingCrop = rect.takeUnless { it.isEmpty })
        }
        return state.copy(draft = null, history = state.history.add(draft))
    }

    private fun finishMove(state: ScreenshotEditorState): ScreenshotEditorState {
        val index = state.selected
        val offset = state.dragOffset
        if (index == null || offset == null) return state.copy(dragOffset = null)
        val image = state.image ?: return state
        val moved = when (val edit = state.history.edits[index].movedBy(offset.x, offset.y)) {
            is Edit.Pixelate -> edit.copy(rect = edit.rect.snappedInto(image.width, image.height))
            else -> edit
        }
        return state.copy(history = state.history.replace(index, moved), dragOffset = null)
    }

    /** Сдвиг обрезки [crop] на [delta], но не за край видимой части картинки [bounds]. */
    private fun cropOffset(crop: ImageRect, delta: ImagePoint, bounds: ImageRect): ImagePoint {
        val moved = crop.movedWithin(delta.x, delta.y, bounds)
        return ImagePoint(moved.left - crop.left, moved.top - crop.top)
    }
}

/** Видимая часть картинки: после обрезки — только она, и новая обрезка или сдвиг не выходят за её край. */
internal val ScreenshotEditorState.bounds: ImageRect
    get() = history.crop ?: ImageRect(0f, 0f, (image?.width ?: 0).toFloat(), (image?.height ?: 0).toFloat())
