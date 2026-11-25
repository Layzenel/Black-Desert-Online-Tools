package com.layzen.bdotools.feature.gameinfo.domain.grid

/** Шаг линий сетки поверх игры в единицах интерфейса игры (UI-px); [Game] совпадает с шагом слотов в игре. */
enum class GridStep(val size: Int) {
    Fine(10),
    Medium(25),
    Game(50),
}
