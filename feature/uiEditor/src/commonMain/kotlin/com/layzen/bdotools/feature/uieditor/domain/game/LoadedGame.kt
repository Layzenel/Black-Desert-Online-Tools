package com.layzen.bdotools.feature.uieditor.domain.game

/**
 * Прочитанный `gamevariable.xml`: все наборы позиций [presets], какой из них сейчас в редакторе ([edited])
 * и исходный файл для последующей записи.
 */
data class LoadedGame(
    val account: String,
    val presets: Map<GamePreset, GameVariable>,
    val file: GameFile,
    val edited: GamePreset = GamePreset.General,
) {
    /** Набор, который сейчас правится в редакторе. */
    val game: GameVariable get() = presets.getValue(edited)
}
