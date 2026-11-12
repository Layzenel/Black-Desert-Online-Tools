package com.layzen.bdotools

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/** Сайт: [App] на всю страницу браузера; заставку из `index.html` убирает, как только Compose запустился. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    document.getElementById("splash")?.remove()
    ComposeViewport {
        App()
    }
}
