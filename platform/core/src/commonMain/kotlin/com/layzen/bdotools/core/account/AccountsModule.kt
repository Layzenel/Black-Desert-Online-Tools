package com.layzen.bdotools.core.account

import org.koin.dsl.module

/** Koin-модуль общего списка аккаунтов: один [GameAccounts] на всё приложение. */
val accountsModule = module {
    single { GameAccounts(createAccountFiles(), createAccountChoice()) }
}
