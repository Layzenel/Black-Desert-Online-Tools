package com.layzen.bdotools.core.keyhint

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Вид подсказок клавиш умений, общий для всего приложения: выбранный раньше или [KeyHints.Default]. */
class AppKeyHints(private val settings: KeyHintSettings) {

    private val mutableSelected = MutableStateFlow(KeyHints.decode(settings.saved()))

    /** Текущий вид; меняется только через [update]. */
    val selected: StateFlow<KeyHints> = mutableSelected.asStateFlow()

    /** Меняет вид через [change] и запоминает результат. */
    fun update(change: (KeyHints) -> KeyHints) {
        mutableSelected.update(change)
        settings.save(mutableSelected.value.encode())
    }
}
