package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
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
        // размер содержимого хранится отдельно: пока окно скрыто, Compose возвращает state.size к размеру окна
        // (журнал 2026-11-21: схема 88×1060, после показа окно снова 560×900), а содержимое не меняется и заново
        // его не задаёт — схема обрезалась после Alt+Tab и при первом показе
        var measured by remember { mutableStateOf<DpSize?>(null) }
        // содержимое меряется без границ окна, и окно получает его размер: длинное комбо помещается целиком
        val fit = Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
            .onSizeChanged { measured = with(density) { DpSize(it.width.toDp(), it.height.toDp()) } }
        LaunchedEffect(shown, measured) {
            val size = measured ?: return@LaunchedEffect
            state.size = size
            if (!shown) return@LaunchedEffect
            // показанное окно — снова под содержимое и внутрь экрана: до показа его размер и место ещё не настоящие
            delay(SETTLE_MS)
            state.size = size
            val width = size.width.value.toInt()
            val height = size.height.value.toInt()
            if (window.width != width || window.height != height) window.setSize(width, height)
            val screen = window.graphicsConfiguration.bounds
            val x = window.x.coerceAtMost(screen.x + screen.width - width).coerceAtLeast(screen.x)
            val y = window.y.coerceAtMost(screen.y + screen.height - height).coerceAtLeast(screen.y)
            if (x != window.x || y != window.y) state.position = WindowPosition(x.dp, y.dp)
        }
        LaunchedEffect(state) {
            snapshotFlow { state.position }.collect { moved -> if (moved is WindowPosition.Absolute) savePosition(id, moved.x, moved.y) }
        }
        WindowDraggableArea(fit) { Box(Modifier.padding(AppTheme.dimens.spaceS)) { content() } }
    }
}

/** Пауза после показа или смены размера окна, прежде чем проверять, не ушло ли оно за край экрана. */
private const val SETTLE_MS = 300L

