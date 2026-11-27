package com.layzen.bdotools.composable.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Что видно в окне холста редактора: масштаб и сдвиг. Это состояние только экрана, поэтому оно живёт в UI, а не во ViewModel.
 * [pan] — сдвиг содержимого в пикселях окна: от 0 до `окно − содержимое`, чтобы за краем содержимого не было пустоты.
 */
@Stable
class CanvasViewport {
    /** Текущий масштаб холста: от [MIN_ZOOM] («всё содержимое в окне») до [MAX_ZOOM]. */
    var zoom by mutableFloatStateOf(MIN_ZOOM)
        private set
    /** Сдвиг содержимого холста в пикселях окна. */
    var pan by mutableStateOf(Offset.Zero)
        private set

    /** Размер окна холста; задаёт его разметка. */
    var size: Size = Size.Zero

    /** Меняет масштаб в [factor] раз так, чтобы точка [focus] окна осталась под курсором. */
    fun zoomAt(focus: Offset, factor: Float) {
        val newZoom = (zoom * factor).coerceIn(MIN_ZOOM, MAX_ZOOM)
        val ratio = newZoom / zoom
        zoom = newZoom
        pan = clamped(focus - (focus - pan) * ratio)
    }

    /** Ставит масштаб [target] относительно середины окна: так его меняет ползунок в панели инструментов. */
    fun zoomTo(target: Float) = zoomAt(Offset(size.width / 2, size.height / 2), target / zoom)

    /** Сдвигает содержимое на [delta] пикселей, не выпуская его за края окна. */
    fun panBy(delta: Offset) {
        pan = clamped(pan + delta)
    }

    /** Точка окна [point] в пикселях содержимого при масштабе 1. */
    fun toContent(point: Offset): Offset = (point - pan) / zoom

    /** Точка содержимого при масштабе 1 в пикселях окна. */
    fun toWindow(point: Offset): Offset = point * zoom + pan

    private fun clamped(value: Offset) = Offset(
        value.x.coerceIn(size.width * (1 - zoom), 0f),
        value.y.coerceIn(size.height * (1 - zoom), 0f),
    )

    companion object {
        /** Масштаб «всё содержимое в окне». */
        const val MIN_ZOOM = 1f

        /** Наибольший масштаб — восьмикратный. */
        const val MAX_ZOOM = 8f
    }
}

/** Запоминает [CanvasViewport] на время жизни экрана; новый [key] — снова «всё в окне». */
@Composable
fun rememberCanvasViewport(key: Any? = Unit): CanvasViewport = remember(key) { CanvasViewport() }
