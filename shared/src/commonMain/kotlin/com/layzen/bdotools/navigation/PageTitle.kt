package com.layzen.bdotools.navigation

import androidx.compose.runtime.Composable

/** Заголовок вкладки браузера: [title] открытой страницы. На Desktop заголовок окна задаёт `main()`, здесь ничего. */
@Composable
internal expect fun PageTitle(title: String)
