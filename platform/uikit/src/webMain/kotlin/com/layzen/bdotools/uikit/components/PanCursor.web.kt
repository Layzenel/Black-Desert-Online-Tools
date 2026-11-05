package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier

/** Кладёт в страницу таблицу стилей с [css] (пустую — убирает правило). */
private fun applyCursorStyle(css: String): Unit = js(
    "(function () { var s = document.getElementById('__bdoPanCursor'); " +
        "if (!s) { s = document.createElement('style'); s.id = '__bdoPanCursor'; document.head.appendChild(s); } " +
        "s.textContent = css; })()",
)

@Composable
actual fun Modifier.panCursor(cursor: PanCursor): Modifier {
    // Compose сам ставит курсор холсту инлайн-стилем, поэтому правило с !important перекрывает его
    DisposableEffect(cursor) {
        applyCursorStyle(
            when (cursor) {
                PanCursor.None -> ""
                PanCursor.Open -> "canvas { cursor: grab !important; }"
                PanCursor.Grabbing -> "canvas { cursor: grabbing !important; }"
            },
        )
        onDispose { applyCursorStyle("") }
    }
    return this
}
