package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.feature.slots.domain.GameVariable

/** Прочитанный `gamevariable.xml`: разобранные данные и исходный файл для последующей записи. */
data class LoadedGame(val account: String, val game: GameVariable, val file: GameFile)
