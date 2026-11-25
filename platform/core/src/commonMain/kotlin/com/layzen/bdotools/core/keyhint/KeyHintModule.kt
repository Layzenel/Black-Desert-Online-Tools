package com.layzen.bdotools.core.keyhint

import org.koin.dsl.module

/** Koin-модуль вида подсказок клавиш: один [AppKeyHints] на всё приложение. */
val keyHintModule = module {
    single { AppKeyHints(createKeyHintSettings()) }
}
