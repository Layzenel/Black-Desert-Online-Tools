package com.layzen.bdotools.core.handoff

import org.koin.dsl.module

/** Koin-модуль передачи картинок между тулзами: один [ImageHandoff] на всё приложение. */
val handoffModule = module {
    single { ImageHandoff() }
}
