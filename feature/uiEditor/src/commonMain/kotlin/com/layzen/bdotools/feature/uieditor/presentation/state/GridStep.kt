package com.layzen.bdotools.feature.uieditor.presentation.state

/** Шаг сетки редактора в UI-px. [Game] совпадает с шагом слотов в игре. */
enum class GridStep(val size: Int) {
    Off(0),
    Fine(10),
    Medium(25),
    Game(50),
}
