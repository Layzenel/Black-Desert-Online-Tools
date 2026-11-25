package com.layzen.bdotools.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.layzen.bdotools.uikit.theme.AppTheme
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import kotlinx.coroutines.delay
import java.awt.Window

private const val DWMWA_CAPTION_COLOR = 35
private const val DWMWA_TEXT_COLOR = 36
private const val HANDLE_RETRIES = 10
private const val HANDLE_RETRY_MS = 50L

/** `dwmapi.dll`: этой функции нет в jna-platform. */
@Suppress("FunctionName")
private interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(hwnd: HWND, attribute: Int, value: IntByReference, size: Int): Int
}

/** Цвет в формате COLORREF Windows: `0x00BBGGRR`. */
private fun Color.toColorRef(): Int {
    val argb = toArgb()
    return (argb and 0xFF) shl 16 or (argb and 0xFF00) or (argb shr 16 and 0xFF)
}

/**
 * Красит системную шапку окна [window] в [caption], текст — в [text], в фокусе и без. Windows 11 (сборка 22000+);
 * на других системах ничего не делает. `false` — у окна ещё нет системного дескриптора (оно ни разу не показывалось).
 */
private fun applyTitleBarColors(window: Window, caption: Color, text: Color): Boolean {
    if (!System.getProperty("os.name").orEmpty().startsWith("Windows")) return true
    val pointer: Pointer = runCatching { Native.getComponentPointer(window) }.getOrNull() ?: return false
    runCatching {
        val dwm = Native.load("dwmapi", DwmApi::class.java)
        val hwnd = HWND(pointer)
        dwm.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, IntByReference(caption.toColorRef()), Int.SIZE_BYTES)
        dwm.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, IntByReference(text.toColorRef()), Int.SIZE_BYTES)
    }
    return true
}

/**
 * Держит шапку окна [window] в цвете акцента выбранной темы окон ([com.layzen.bdotools.uikit.theme.WindowThemeStyle.titleBarTop])
 * и светлом тексте; переприменяет при смене темы и когда окно показывают (скрытое в трее окно дескриптора может не иметь).
 */
@Composable
internal fun TitleBarColor(window: Window, visible: Boolean) {
    val caption = AppTheme.window.titleBarTop
    val text = AppTheme.colors.textPrimary
    LaunchedEffect(window, caption, text, visible) {
        repeat(HANDLE_RETRIES) {
            if (applyTitleBarColors(window, caption, text)) return@LaunchedEffect
            delay(HANDLE_RETRY_MS)
        }
    }
}
