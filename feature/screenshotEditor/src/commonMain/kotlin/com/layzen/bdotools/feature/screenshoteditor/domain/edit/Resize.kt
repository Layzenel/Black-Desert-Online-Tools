package com.layzen.bdotools.feature.screenshoteditor.domain.edit

/**
 * Начало изменения размера: за какую ручку [handle] взяли правку и какой прямоугольник [bounds] она занимала.
 * Размер надписи меряет экран, поэтому прямоугольник приходит снаружи.
 */
data class Resize(val handle: ResizeHandle, val bounds: ImageRect)
