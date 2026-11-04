package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.feature.slots.domain.GamePreset
import com.layzen.bdotools.feature.slots.domain.GameVariable

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
