package com.layzen.bdotools.feature.screenshoteditor.domain.edit

/**
 * Фигура рамки, вписанная в её прямоугольник. [Marker] — не контур, а полупрозрачная заливка, как выделение маркером.
 */
enum class FrameShape : MarkStyle { Rectangle, Ellipse, Rhombus, Triangle, Star, Marker }
