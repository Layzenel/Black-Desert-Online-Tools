package com.layzen.bdotools.composable.overlay

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import java.awt.Window

private const val WS_EX_TRANSPARENT = 0x00000020
private const val WS_EX_TOOLWINDOW = 0x00000080
private const val WS_EX_NOACTIVATE = 0x08000000

/**
 * [enabled] — клики и колесо мыши проходят сквозь окно [window] в окно под ним (игру); выключено — окно ловит мышь,
 * его можно перетаскивать. Окно не попадает в Alt+Tab и панель задач. Только Windows; на других системах ничего не делает.
 */
fun setClickThrough(window: Window, enabled: Boolean) {
    if (!System.getProperty("os.name").orEmpty().startsWith("Windows")) return
    val hwnd = HWND(Native.getComponentPointer(window))
    val style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE) or WinUser.WS_EX_LAYERED or WS_EX_TOOLWINDOW or WS_EX_NOACTIVATE
    User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, if (enabled) style or WS_EX_TRANSPARENT else style and WS_EX_TRANSPARENT.inv())
}
