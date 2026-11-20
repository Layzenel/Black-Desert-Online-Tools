package com.layzen.bdotools.feature.comboassist.data.keys

import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef

/** `RAWINPUTDEVICE`: какое устройство слушать ([usUsagePage]/[usUsage]) и какому окну слать `WM_INPUT`. */
@Structure.FieldOrder("usUsagePage", "usUsage", "dwFlags", "hwndTarget")
internal class RawInputDevice : Structure() {
    /** Страница назначения HID: 1 — обычные устройства рабочего стола. */
    @JvmField var usUsagePage: Short = 0

    /** Назначение HID: 2 — мышь, 6 — клавиатура. */
    @JvmField var usUsage: Short = 0

    /** Флаги `RIDEV_*`. */
    @JvmField var dwFlags: Int = 0

    /** Окно, которому идут `WM_INPUT`. */
    @JvmField var hwndTarget: WinDef.HWND? = null
}
