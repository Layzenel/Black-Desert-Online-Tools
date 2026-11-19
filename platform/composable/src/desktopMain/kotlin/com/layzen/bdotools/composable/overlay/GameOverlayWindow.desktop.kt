package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.uikit.theme.AppTheme

/** Окно поверх игры в настольной версии; место окна [id] — в `Preferences` пользователя ([savePosition]). */
@Composable
actual fun GameOverlayWindow(id: String, anchor: OverlayAnchor, size: DpSize, content: @Composable () -> Unit) {
    val state = rememberWindowState(position = remember { savedPosition(id, anchor, size) }, size = size)
    val shown by rememberGameActive()
    // кнопка меню поднимается над окном после каждого его появления
    LaunchedEffect(shown) { if (shown) OverlayLayers.overlayShown++ }
    Window(
        onCloseRequest = {},
        state = state,
        visible = shown,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        // тянуть можно, только пока курсор показан: атака со спрятанным курсором окно не сдвинет
        val cursorShown by rememberCursorShown()
        LaunchedEffect(window, cursorShown) { setClickThrough(window, enabled = !cursorShown) }
        val density = LocalDensity.current
        // содержимое меряется без границ окна, и окно получает его размер: длинное комбо помещается целиком
        val fit = Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
            .onSizeChanged { state.size = with(density) { DpSize(it.width.toDp(), it.height.toDp()) } }
        LaunchedEffect(state) {
            snapshotFlow { state.position }.collect { moved -> if (moved is WindowPosition.Absolute) savePosition(id, moved.x, moved.y) }
        }
        WindowDraggableArea(fit) { Box(Modifier.padding(AppTheme.dimens.spaceS)) { content() } }
    }
}
