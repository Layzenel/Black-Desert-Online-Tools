package com.layzen.bdotools.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import kotlinx.browser.document

@Composable
internal actual fun PageTitle(title: String) {
    SideEffect { document.title = title }
}
