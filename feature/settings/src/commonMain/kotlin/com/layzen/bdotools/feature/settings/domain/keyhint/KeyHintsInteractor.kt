package com.layzen.bdotools.feature.settings.domain.keyhint

import com.layzen.bdotools.core.keyhint.AppKeyHints
import com.layzen.bdotools.core.keyhint.KeyHints
import kotlinx.coroutines.flow.StateFlow

/** Выбор вида подсказок клавиш умений (слово, сокращение, картинка) для всего приложения. */
class KeyHintsInteractor(private val keyHints: AppKeyHints) {
    /** Текущий вид подсказок. */
    val selected: StateFlow<KeyHints> get() = keyHints.selected

    /** Показывает клавиши по [hints] и запоминает выбор. */
    fun select(hints: KeyHints) = keyHints.update { hints }
}
