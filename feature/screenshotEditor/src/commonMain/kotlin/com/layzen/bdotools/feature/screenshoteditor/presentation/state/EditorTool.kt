package com.layzen.bdotools.feature.screenshoteditor.presentation.state

/** Инструмент, которым правят скриншот мышью. [usesColor] — у инструмента есть цвет и толщина. */
enum class EditorTool(val usesColor: Boolean) {
    Crop(false),
    Pixelate(false),
    Arrow(true),
    Frame(true),
    Brush(true),
    Text(true),
    Counter(true),
}
