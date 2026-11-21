package com.layzen.bdotools.feature.gameinfo.data

import com.sun.jna.Callback
import com.sun.jna.CallbackReference
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.Guid
import com.sun.jna.ptr.LongByReference

/** Функции ETW из `advapi32.dll`: своя сессия трассировки событий и чтение её на лету. */
@Suppress("FunctionName")
private interface Etw : Library {
    fun StartTraceW(handle: LongByReference, name: WString, properties: Pointer): Int
    fun ControlTraceW(handle: Long, name: WString?, properties: Pointer, code: Int): Int
    fun EnableTraceEx2(
        handle: Long, provider: Guid.GUID, code: Int, level: Byte,
        matchAny: Long, matchAll: Long, timeout: Int, parameters: Pointer?,
    ): Int
    fun OpenTraceW(logfile: Pointer): Long
    fun ProcessTrace(handles: LongArray, count: Int, start: Pointer?, end: Pointer?): Int
    fun CloseTrace(handle: Long): Int
}

private val etw: Etw by lazy { Native.load("advapi32", Etw::class.java) }

/** Обработчик события ETW: получает `EVENT_RECORD`. */
private fun interface EventRecordCallback : Callback {
    fun invoke(record: Pointer)
}

/**
 * Сессия ETW с событиями `Present_Info` провайдера `Microsoft-Windows-DxgKrnl`: ядро видеоподсистемы пишет такое
 * событие на каждый кадр, который процесс отдаёт на экран, — по ним считают FPS Xbox Game Bar и PresentMon. Провайдер
 * `Microsoft-Windows-DXGI` не годится: номера его событий меняются от сборки к сборке Windows (в 26300 нет `Present_Start`,
 * 42). В игру ничего не внедряется.
 * Запустить сессию может только администратор. События читаются в своём потоке, пока сессию не остановят ([stop]).
 */
internal class PresentTrace private constructor(private val session: Long) {
    private val frames = ArrayDeque<Long>()
    private var lastFrameNanos = 0L
    private var consumer = INVALID_HANDLE

    // ссылка держит обработчик, пока ETW его вызывает: иначе сборщик мусора уберёт его из-под системы
    private val callback = EventRecordCallback(::onEvent)

    /** Процесс, чьи кадры считаются; смена процесса сбрасывает счёт. */
    @Volatile
    var processId: Long? = null
        set(value) {
            if (value != field) synchronized(frames) { frames.clear() }
            field = value
        }

    /**
     * Моменты вывода кадров [processId] за последние [WINDOW_TICKS] (мс от первого кадра окна, по возрастанию);
     * `null` — кадров не было [STALE_NANOS] (игра свёрнута или закрыта).
     */
    fun frames(): List<Double>? = synchronized(frames) {
        if (frames.isEmpty() || System.nanoTime() - lastFrameNanos >= STALE_NANOS) return null
        val first = frames.first()
        frames.map { (it - first) / TICKS_PER_MS }
    }

    /** Останавливает сессию: поток чтения завершается сам. */
    fun stop() {
        etw.ControlTraceW(session, null, properties(), CONTROL_STOP)
        etw.CloseTrace(consumer)
    }

    private fun openConsumer(): Boolean {
        val name = wide(SESSION_NAME)
        val logfile = Memory(LOGFILE_SIZE).apply {
            clear()
            setPointer(LOGFILE_LOGGER_NAME, name)
            setInt(LOGFILE_MODE, PROCESS_TRACE_MODE_REAL_TIME or PROCESS_TRACE_MODE_EVENT_RECORD)
            setPointer(LOGFILE_CALLBACK, CallbackReference.getFunctionPointer(callback))
        }
        consumer = etw.OpenTraceW(logfile)
        if (consumer == INVALID_HANDLE) return false
        Thread({ etw.ProcessTrace(longArrayOf(consumer), 1, null, null) }, "bdo-tools-fps").apply { isDaemon = true }.start()
        return true
    }

    private fun onEvent(record: Pointer) {
        if (record.getShort(EVENT_ID).toInt() != PRESENT_INFO) return
        if (record.getInt(EVENT_PROCESS_ID).toLong() != processId) return
        val time = record.getLong(EVENT_TIMESTAMP)
        synchronized(frames) {
            frames.addLast(time)
            while (time - frames.first() >= WINDOW_TICKS) frames.removeFirst()
            lastFrameNanos = System.nanoTime()
        }
    }

    companion object {
        /** Запускает сессию; `null` — не вышло (нет прав администратора или Windows отказала). */
        fun start(): PresentTrace? {
            val handle = LongByReference()
            var status = etw.StartTraceW(handle, WString(SESSION_NAME), properties())
            if (status == ERROR_ALREADY_EXISTS) {
                // сессия осталась от программы, закрытой аварийно: читать её больше некому
                etw.ControlTraceW(0, WString(SESSION_NAME), properties(), CONTROL_STOP)
                status = etw.StartTraceW(handle, WString(SESSION_NAME), properties())
            }
            if (status != 0) return null
            val trace = PresentTrace(handle.value)
            val enabled = etw.EnableTraceEx2(handle.value, DXGKRNL, CONTROL_CODE_ENABLE, LEVEL_INFORMATION, KEYWORD_PRESENT, 0, 0, null) == 0
            if (enabled && trace.openConsumer()) return trace
            trace.stop()
            return null
        }

        /** `EVENT_TRACE_PROPERTIES` сессии на лету с именем после структуры; ETW меняет её, поэтому каждый раз новая. */
        private fun properties(): Memory = Memory(PROPERTIES_SIZE + NAME_BYTES).apply {
            clear()
            setInt(0, (PROPERTIES_SIZE + NAME_BYTES).toInt())
            setInt(WNODE_CLIENT_CONTEXT, CLOCK_SYSTEM_TIME)
            setInt(WNODE_FLAGS, WNODE_FLAG_TRACED_GUID)
            setInt(BUFFER_SIZE_KB, BUFFER_KB)
            setInt(LOG_FILE_MODE, EVENT_TRACE_REAL_TIME_MODE)
            setInt(FLUSH_TIMER, 1)
            setInt(LOGGER_NAME_OFFSET, PROPERTIES_SIZE.toInt())
        }

        private fun wide(text: String): Memory = Memory((text.length + 1) * 2L).apply { setWideString(0, text) }

        private const val SESSION_NAME = "BDO Tools FPS"
        private val DXGKRNL = Guid.GUID.fromString("{802EC45A-1E99-4B83-9920-87C98277BA9D}")

        /** Ключевое слово `Present` у `Microsoft-Windows-DxgKrnl` (как у PresentMon): кадры и их история. */
        private const val KEYWORD_PRESENT = 0x8000000L

        /** `Present_Info` у `Microsoft-Windows-DxgKrnl`: кадр отдан на экран. */
        private const val PRESENT_INFO = 184

        /** Событий от всех программ — тысячи в секунду (замер 2026-11-21): буферы крупнее, чтобы не терять. */
        private const val BUFFER_KB = 64

        // время событий — системное, в 100 нс; окно — для «1% low», фризов и графика
        private const val TICKS_PER_MS = 10_000.0
        private const val WINDOW_TICKS = 50_000_000L
        private const val STALE_NANOS = 3_000_000_000L
        private const val INVALID_HANDLE = -1L
        private const val ERROR_ALREADY_EXISTS = 183
        private const val CONTROL_STOP = 1
        private const val CONTROL_CODE_ENABLE = 1
        private const val LEVEL_INFORMATION: Byte = 4

        // EVENT_TRACE_PROPERTIES (x64)
        private const val PROPERTIES_SIZE = 120L
        private const val NAME_BYTES = 1024L
        private const val WNODE_CLIENT_CONTEXT = 40L
        private const val WNODE_FLAGS = 44L
        private const val BUFFER_SIZE_KB = 48L
        private const val LOG_FILE_MODE = 64L
        private const val FLUSH_TIMER = 68L
        private const val LOGGER_NAME_OFFSET = 116L
        private const val CLOCK_SYSTEM_TIME = 2
        private const val WNODE_FLAG_TRACED_GUID = 0x00020000
        private const val EVENT_TRACE_REAL_TIME_MODE = 0x100

        // EVENT_TRACE_LOGFILEW (x64)
        private const val LOGFILE_SIZE = 448L
        private const val LOGFILE_LOGGER_NAME = 8L
        private const val LOGFILE_MODE = 28L
        private const val LOGFILE_CALLBACK = 424L
        private const val PROCESS_TRACE_MODE_REAL_TIME = 0x100
        private const val PROCESS_TRACE_MODE_EVENT_RECORD = 0x10000000

        // EVENT_RECORD: EVENT_HEADER в начале
        private const val EVENT_PROCESS_ID = 12L
        private const val EVENT_TIMESTAMP = 16L
        private const val EVENT_ID = 40L
    }
}
