package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/** Кладёт в страницу свою таблицу стилей курсора растягивания (пустую — убирает правило). */
private fun applyResizeCursorStyle(css: String): Unit = js(
    "(function () { var s = document.getElementById('__bdoResizeCursor'); " +
        "if (!s) { s = document.createElement('style'); s.id = '__bdoResizeCursor'; document.head.appendChild(s); } " +
        "s.textContent = css; })()",
)

@Composable
actual fun Modifier.resizeCursor(dragging: Boolean): Modifier {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val active = hovered || dragging
    // Compose сам ставит курсор холсту инлайн-стилем, поэтому правило с !important перекрывает его
    DisposableEffect(active) {
        applyResizeCursorStyle(if (active) "canvas { cursor: nesw-resize !important; }" else "")
        onDispose { applyResizeCursorStyle("") }
    }
    return hoverable(source)
}
