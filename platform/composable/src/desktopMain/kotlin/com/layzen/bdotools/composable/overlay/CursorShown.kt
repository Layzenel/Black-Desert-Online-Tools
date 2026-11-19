package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.sun.jna.Native
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary

private const val CURSOR_SHOWING = 0x1

/** `CURSORINFO` из WinAPI: в jna-platform его нет. JNA заполняет поля через рефлексию, поэтому они `@JvmField`. */
@Structure.FieldOrder("cbSize", "flags", "hCursor", "ptScreenPos")
internal class CursorInfo : Structure() {
    /** Размер структуры: WinAPI проверяет его. */
    @JvmField var cbSize: Int = 0

    /** `CURSOR_SHOWING`, если курсор показан. */
    @JvmField var flags: Int = 0

    /** Текущий курсор. */
    @JvmField var hCursor: Pointer? = null

    /** Положение курсора на экране. */
    @JvmField var ptScreenPos: WinDef.POINT = WinDef.POINT()

    init {
        cbSize = size()
    }
}

/** Нужная часть `user32`: только `GetCursorInfo`. */
private interface CursorUser32 : StdCallLibrary {
    fun GetCursorInfo(info: CursorInfo): Boolean

    companion object {
        val INSTANCE: CursorUser32 = Native.load("user32", CursorUser32::class.java)
    }
}

/** Как часто проверять, показан ли курсор. */
private const val CURSOR_CHECK_MS = 100L

/**
 * Показан ли курсор ([isCursorShown]), с проверкой раз в [CURSOR_CHECK_MS]: окна, которые ловят мышь, пока курсор
 * спрятан, пропускают клики в игру — атака в режиме боя не достаётся оверлею.
 */
@Composable
fun rememberCursorShown(): State<Boolean> = produceState(true) {
    while (true) {
        value = withContext(Dispatchers.IO) { isCursorShown() }
        delay(CURSOR_CHECK_MS)
    }
}

/**
 * Показан ли системный курсор. В режиме боя игра его прячет, но положение остаётся прежним — без этой проверки
 * спрятанный курсор над слотом вызывал бы тултип. Только чтение состояния, без хуков ввода. Не Windows — всегда `true`.
 */
fun isCursorShown(): Boolean {
    if (!System.getProperty("os.name").orEmpty().startsWith("Windows")) return true
    val info = CursorInfo()
    return !CursorUser32.INSTANCE.GetCursorInfo(info) || info.flags and CURSOR_SHOWING != 0
}
