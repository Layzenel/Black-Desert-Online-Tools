package com.layzen.bdotools.feature.comboassist.data.keys

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import java.util.concurrent.atomic.AtomicInteger

private const val WM_INPUT = 0x00FF
private const val RID_INPUT = 0x10000003
private const val RIDEV_INPUTSINK = 0x00000100
private const val TYPE_MOUSE = 0
private const val TYPE_KEYBOARD = 1
private const val USAGE_PAGE_DESKTOP: Short = 1
private const val USAGE_MOUSE: Short = 2
private const val USAGE_KEYBOARD: Short = 6
private const val JOIN_MS = 1000L

/**
 * Слушает клавиатуру и кнопки мыши через Raw Input с `RIDEV_INPUTSINK`, пока не закрыт: Windows шлёт копию ввода
 * скрытому окну программы, даже когда на переднем плане игра. Ввод не перехватывается и не задерживается (это не
 * хук): игра получает его как обычно. [onKey] зовётся из своего потока.
 *
 * Игра запущена от администратора, и Windows не отдаёт её ввод процессу с меньшими правами: без запуска программы
 * от администратора (или UIAccess) нажатий в игре не будет. [onStarted] сообщает, удалось ли подписаться на ввод
 * (не Windows — `false`).
 */
internal class RawInputListener(onStarted: (Boolean) -> Unit, onKey: (RawKey) -> Unit) : AutoCloseable {
    private val threadId = AtomicInteger()
    private val thread: Thread?

    init {
        thread = if (!System.getProperty("os.name").orEmpty().startsWith("Windows")) {
            onStarted(false)
            null
        } else {
            Thread({ run(onStarted, onKey) }, "raw-input").apply { isDaemon = true; start() }
        }
    }

    override fun close() {
        threadId.get().takeIf { it != 0 }?.let { User32.INSTANCE.PostThreadMessage(it, WinUser.WM_QUIT, null, null) }
        thread?.join(JOIN_MS)
    }

    /** Почему слушать не вышло (для журнала) или `null`. */
    @Volatile var failure: String? = null
        private set

    private fun run(onStarted: (Boolean) -> Unit, onKey: (RawKey) -> Unit) {
        var started = false
        // ошибку потока иначе никто не увидит: поток свой, и слежение просто осталось бы без нажатий
        try {
            // окно не показывается: оно нужно только как адресат WM_INPUT; с ним у потока появляется очередь сообщений
            val window = User32.INSTANCE.CreateWindowEx(0, "STATIC", "BDO Tools input", 0, 0, 0, 0, 0, null, null, null, null)
            threadId.set(Kernel32.INSTANCE.GetCurrentThreadId())
            if (window == null) failure = "CreateWindowEx error ${Kernel32.INSTANCE.GetLastError()}"
            val ok = window != null && register(window)
            if (window != null && !ok) failure = "RegisterRawInputDevices error ${Kernel32.INSTANCE.GetLastError()}"
            started = true
            onStarted(ok)
            if (ok) loop(onKey)
            window?.let { User32.INSTANCE.DestroyWindow(it) }
        } catch (e: Throwable) {
            failure = e.toString()
            if (!started) onStarted(false)
        }
    }

    private fun register(window: WinDef.HWND): Boolean {
        @Suppress("UNCHECKED_CAST")
        val devices = RawInputDevice().toArray(2) as Array<RawInputDevice>
        devices[0].apply { usUsagePage = USAGE_PAGE_DESKTOP; usUsage = USAGE_KEYBOARD; dwFlags = RIDEV_INPUTSINK; hwndTarget = window }
        devices[1].apply { usUsagePage = USAGE_PAGE_DESKTOP; usUsage = USAGE_MOUSE; dwFlags = RIDEV_INPUTSINK; hwndTarget = window }
        return RawInputApi.INSTANCE.RegisterRawInputDevices(devices, devices.size, devices[0].size())
    }

    /** Цикл сообщений потока до `WM_QUIT`: каждое `WM_INPUT` разбирается в [onKey]. */
    private fun loop(onKey: (RawKey) -> Unit) {
        val message = WinUser.MSG()
        while (User32.INSTANCE.GetMessage(message, null, 0, 0) > 0) {
            if (message.message == WM_INPUT) read(WinNT.HANDLE(Pointer(message.lParam.toLong()))).forEach(onKey)
            User32.INSTANCE.TranslateMessage(message)
            User32.INSTANCE.DispatchMessage(message)
        }
    }

    /** Клавиши события `WM_INPUT` [rawInput]: заголовок `RAWINPUTHEADER`, за ним `RAWKEYBOARD` или `RAWMOUSE`. */
    private fun read(rawInput: WinNT.HANDLE): List<RawKey> {
        val headerSize = 8 + 2 * Native.POINTER_SIZE
        val size = IntByReference()
        RawInputApi.INSTANCE.GetRawInputData(rawInput, RID_INPUT, null, size, headerSize)
        if (size.value <= headerSize) return emptyList()
        val data = Memory(size.value.toLong())
        if (RawInputApi.INSTANCE.GetRawInputData(rawInput, RID_INPUT, data, size, headerSize) <= 0) return emptyList()
        return when (data.getInt(0)) {
            TYPE_KEYBOARD -> listOfNotNull(keyboardKey(data.ushort(headerSize + 2), data.ushort(headerSize + 6)))
            TYPE_MOUSE -> mouseKeys(data.ushort(headerSize + 4))
            else -> emptyList()
        }
    }

    private fun Memory.ushort(offset: Int): Int = getShort(offset.toLong()).toInt() and 0xFFFF
}
