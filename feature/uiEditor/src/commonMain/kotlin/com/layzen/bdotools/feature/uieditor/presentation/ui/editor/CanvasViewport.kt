package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/** Масштаб холста: от «весь экран игры в окне» до восьмикратного. */
private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 8f

/**
 * Что видно в окне холста: масштаб и сдвиг. Это состояние только экрана, поэтому оно живёт в UI, а не во ViewModel.
 * [pan] — сдвиг содержимого в пикселях окна: от 0 до `окно − содержимое`, чтобы за краем экрана игры не было пустоты.
 */
@Stable
internal class CanvasViewport {
    var zoom by mutableFloatStateOf(MIN_ZOOM)
        private set
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

    fun panBy(delta: Offset) {
        pan = clamped(pan + delta)
    }

    private fun clamped(value: Offset) = Offset(
        value.x.coerceIn(size.width * (1 - zoom), 0f),
        value.y.coerceIn(size.height * (1 - zoom), 0f),
    )
}

@Composable
internal fun rememberCanvasViewport(): CanvasViewport = remember { CanvasViewport() }
