package com.layzen.bdotools.feature.comboassist.data.keys

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

/** Функции Raw Input из `user32.dll`, которых нет в JNA. */
internal interface RawInputApi : StdCallLibrary {
    /** Подписывает окна на `WM_INPUT` от устройств [devices]. */
    fun RegisterRawInputDevices(devices: Array<RawInputDevice>, count: Int, size: Int): Boolean

    /** Данные `WM_INPUT` [rawInput] в [data] (`null` — только размер в [size]). */
    fun GetRawInputData(rawInput: WinNT.HANDLE, command: Int, data: Pointer?, size: IntByReference, headerSize: Int): Int

    companion object {
        /** Загруженная `user32.dll`. */
        val INSTANCE: RawInputApi by lazy { Native.load("user32", RawInputApi::class.java, W32APIOptions.DEFAULT_OPTIONS) }
    }
}
