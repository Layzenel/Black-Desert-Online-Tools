@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * Один раз на страницу вешает слушатели клавиатуры: флаг `__bdoAlt` — зажат ли Alt. Отпускание Alt не доходит
 * до браузера (`preventDefault`), иначе Firefox открывает строку меню; потеря фокуса окном сбрасывает флаг.
 */
private fun installAltTracker(): Unit = js(
    "(function () { if (globalThis.__bdoAltTracker) return; globalThis.__bdoAltTracker = true; globalThis.__bdoAlt = false; " +
        "var track = function (e) { globalThis.__bdoAlt = e.altKey; if (e.key === 'Alt') e.preventDefault(); }; " +
        "window.addEventListener('keydown', track, true); window.addEventListener('keyup', track, true); " +
        "window.addEventListener('mousemove', function (e) { globalThis.__bdoAlt = e.altKey; }, true); " +
        "window.addEventListener('blur', function () { globalThis.__bdoAlt = false; }); })()",
)

private fun altFlag(): Boolean = js("globalThis.__bdoAlt === true")

@Composable
internal actual fun rememberAltPressed(): Boolean {
    var pressed by remember { mutableStateOf(false) }
    // Флаг читается раз в кадр: колбэк из JS в Kotlin у js и wasm устроен по-разному, а опрос одинаков
    LaunchedEffect(Unit) {
        installAltTracker()
        while (true) {
            withFrameNanos { }
            pressed = altFlag()
        }
    }
    return pressed
}
