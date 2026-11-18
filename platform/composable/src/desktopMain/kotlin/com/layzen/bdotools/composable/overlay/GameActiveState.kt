package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Как часто проверять, что игра на переднем плане. */
private const val FOREGROUND_CHECK_MS = 300L

/** Игра на переднем плане ([isGameForeground]), с проверкой раз в [FOREGROUND_CHECK_MS]. */
@Composable
fun rememberGameActive(): State<Boolean> = produceState(false) {
    while (true) {
        value = withContext(Dispatchers.IO) { isGameForeground() }
        delay(FOREGROUND_CHECK_MS)
    }
}
