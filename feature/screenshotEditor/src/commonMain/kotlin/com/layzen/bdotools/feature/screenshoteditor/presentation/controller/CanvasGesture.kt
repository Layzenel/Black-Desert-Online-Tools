package com.layzen.bdotools.feature.screenshoteditor.presentation.controller

import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Resize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedBy
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.movedWithin
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.resized
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.snappedInto
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.within
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/**
 * Одно нажатие мыши на холсте — от нажатия до отпускания. Что оно делает, решается при нажатии:
 * - набирают надпись: нажатие только заканчивает её;
 * - есть обведённая обрезка: нажатие в ней тянет её, мимо — отменяет её;
 * - над ручкой выбранной правки: тянет ручку — меняет размер;
 * - над правкой: выбирает её и тянет;
 * - включена пипетка: берёт цвет картинки под мышью;
 * - иначе: рисует выбранным инструментом и снимает выбор; клик инструментом «Текст» открывает поле надписи,
 *   инструментом «Номер» — ставит кружок со следующим номером. Нарисованное сразу выбрано.
 *
 * Пока тянут, правка видна на новом месте, а в историю попадает одним шагом, когда отпустят.
 */
internal class CanvasGesture {
    private enum class Mode { Draw, MoveSelected, Resize, MoveCrop, CropCancelled, TextFinished, Picked }

    private var mode: Mode? = null
    private var start: ImagePoint? = null
    private var resize: Resize? = null

    /** Нажали в [at] над правкой номер [hit] (`null` — не над правкой) или над ручкой выбранной правки ([grab]). */
    fun press(state: ScreenshotEditorState, at: ImagePoint, hit: Int?, grab: Resize?): ScreenshotEditorState {
        start = at
        resize = grab
        val crop = state.pendingCrop
        val (next, result) = when {
            state.picking -> Mode.Picked to state.withPickedColor(at)
            state.textAt != null -> Mode.TextFinished to state.withTypedLabel()
            crop != null && at in crop -> Mode.MoveCrop to state
            crop != null -> Mode.CropCancelled to state.copy(pendingCrop = null)
            grab != null && state.selected != null -> Mode.Resize to state
            hit != null -> Mode.MoveSelected to state.copy(selected = hit).withSelectionStyle()
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
            Mode.Resize -> resizedTo(state, to)
            Mode.MoveCrop -> state.copy(dragOffset = state.pendingCrop?.let { cropOffset(it, delta, state.bounds) })
            Mode.CropCancelled, Mode.TextFinished, Mode.Picked, null -> state
        }
    }

    /** Кнопку отпустили: правка из черновика, сдвиг или новый размер — в историю, обведённая обрезка ждёт галочки. */
    fun release(state: ScreenshotEditorState): ScreenshotEditorState = when (mode) {
        Mode.Draw -> finishDraw(state)
        Mode.MoveSelected -> finishMove(state)
        Mode.Resize -> finishResize(state)
        Mode.MoveCrop -> state.copy(pendingCrop = state.shownCrop, dragOffset = null)
        Mode.CropCancelled, Mode.TextFinished, Mode.Picked, null -> state
    }

    /** Отпустили, не сдвинув мышь: клик по обведённой обрезке отменяет её, клик инструментом «Текст» открывает поле надписи. */
    fun tap(state: ScreenshotEditorState, at: ImagePoint): ScreenshotEditorState {
        val tapped = mode
        mode = null
        start = null
        resize = null
        return when {
            tapped == Mode.MoveCrop -> state.copy(pendingCrop = null)
            tapped == Mode.Draw && state.tool == EditorTool.Text -> state.copy(textAt = at, text = "")
            tapped == Mode.Draw && state.tool == EditorTool.Counter -> state.counterAt(at)?.let(state::withAdded) ?: state
            else -> state
        }
    }

    private fun resizedTo(state: ScreenshotEditorState, to: ImagePoint): ScreenshotEditorState {
        val grab = resize ?: return state
        val edit = state.selected?.let(state.history.edits::getOrNull) ?: return state
        return state.copy(resized = edit.resized(grab, to))
    }

    private fun finishDraw(state: ScreenshotEditorState): ScreenshotEditorState {
        val draft = state.draft?.takeIf(Edit::isMeaningful) ?: return state.copy(draft = null)
        if (draft is Edit.Crop) {
            val rect = draft.rect.within(state.bounds)
            return state.copy(draft = null, pendingCrop = rect.takeUnless { it.isEmpty })
        }
        return state.copy(draft = null).withAdded(draft)
    }

    private fun finishMove(state: ScreenshotEditorState): ScreenshotEditorState {
        val index = state.selected
        val offset = state.dragOffset
        if (index == null || offset == null) return state.copy(dragOffset = null)
        return state.withReplaced(index, state.history.edits[index].movedBy(offset.x, offset.y)).copy(dragOffset = null)
    }

    private fun finishResize(state: ScreenshotEditorState): ScreenshotEditorState {
        val index = state.selected
        val edit = state.resized?.takeIf(Edit::isMeaningful)
        if (index == null || edit == null) return state.copy(resized = null)
        return state.withReplaced(index, edit).copy(resized = null)
    }

    private fun ScreenshotEditorState.withReplaced(index: Int, edit: Edit): ScreenshotEditorState {
        val image = image ?: return this
        return copy(history = history.replace(index, edit.snappedInto(image.width, image.height)))
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

/** Новая пометка [edit] — в истории и сразу выбрана, как если бы по ней кликнули. */
internal fun ScreenshotEditorState.withAdded(edit: Edit): ScreenshotEditorState =
    copy(history = history.add(edit), selected = history.edits.size)
