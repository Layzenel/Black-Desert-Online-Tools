package com.layzen.bdotools.core.language

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Язык интерфейса, общий для всего приложения. При запуске берётся выбранный раньше язык, без него — язык
 * системы, а если приложение его не знает — [AppLanguage.Fallback].
 */
class AppLanguages(private val settings: LanguageSettings) {

    private val mutableSelected = MutableStateFlow(
        AppLanguage.fromTag(settings.saved()) ?: AppLanguage.fromTag(settings.system()) ?: AppLanguage.Fallback,
    )

    /** Текущий язык интерфейса; меняется только через [select]. */
    val selected: StateFlow<AppLanguage> = mutableSelected.asStateFlow()

    /** Переключает интерфейс на [language] и запоминает выбор. */
    fun select(language: AppLanguage) {
        settings.save(language.tag)
        mutableSelected.value = language
    }
}
