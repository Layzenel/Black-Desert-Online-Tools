package com.layzen.bdotools.core.language

import org.koin.dsl.module

/** Koin-модуль языка интерфейса: один [AppLanguages] на всё приложение. */
val languageModule = module {
    single { AppLanguages(createLanguageSettings()) }
}
