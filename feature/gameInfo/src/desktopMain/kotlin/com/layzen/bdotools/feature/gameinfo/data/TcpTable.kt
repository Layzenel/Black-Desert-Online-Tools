package com.layzen.bdotools.feature.gameinfo.data

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import java.net.InetAddress

/** Функции `iphlpapi.dll`: таблица TCP-подключений и их статистика. */
@Suppress("FunctionName")
private interface IpHelper : Library {
    fun GetExtendedTcpTable(table: Pointer?, size: IntByReference, order: Boolean, family: Int, tableClass: Int, reserved: Int): Int

    fun GetPerTcpConnectionEStats(
        row: Pointer, type: Int,
        rw: Pointer?, rwVersion: Int, rwSize: Int,
        ros: Pointer?, rosVersion: Int, rosSize: Int,
        rod: Pointer?, rodVersion: Int, rodSize: Int,
    ): Int
}

private val ipHelper: IpHelper by lazy { Native.load("iphlpapi", IpHelper::class.java) }

/**
 * TCP-подключение процесса.
 *
 * @property row сырая запись `MIB_TCPROW` (состояние, адреса и порты в сетевом порядке) — для запроса статистики
 * @property remote удалённый адрес
 * @property remotePort удалённый порт
 */
internal class TcpConnection(val row: IntArray, val remote: InetAddress, val remotePort: Int)

/** Установленные IPv4-подключения процесса [pid] из `GetExtendedTcpTable` (класс `TCP_TABLE_OWNER_PID_ALL`). */
internal fun tcpConnections(pid: Long): List<TcpConnection> {
    val size = IntByReference(0)
    ipHelper.GetExtendedTcpTable(null, size, false, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0)
    if (size.value <= 0) return emptyList()
    val table = Memory(size.value.toLong())
    if (ipHelper.GetExtendedTcpTable(table, size, false, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0) != 0) return emptyList()
    return (0 until table.getInt(0)).mapNotNull { i ->
        val offset = 4L + i * ROW_SIZE
        val fields = table.getIntArray(offset, ROW_FIELDS)
        if (fields[5].toLong() != pid || fields[0] != STATE_ESTABLISHED) return@mapNotNull null
        val address = InetAddress.getByAddress(table.getByteArray(offset + REMOTE_ADDRESS_OFFSET, IPV4_BYTES))
        TcpConnection(fields.copyOf(MIB_TCPROW_FIELDS), address, networkPort(fields[4]))
    }
}

/**
 * Сглаженная задержка подключения [connection] по статистике Windows, мс, или `null`, если её не собирают. Включить
 * сбор может только администратор; его включает, например, открытый «Монитор ресурсов» — тогда читать можно всем.
 */
internal fun smoothedRtt(connection: TcpConnection): Int? {
    val row = Memory(MIB_TCPROW_FIELDS * 4L).apply { write(0, connection.row, 0, MIB_TCPROW_FIELDS) }
    val rw = Memory(1).apply { setByte(0, 0) }
    val rod = Memory(PATH_ROD_SIZE)
    val result = ipHelper.GetPerTcpConnectionEStats(row, ESTATS_PATH, rw, 0, 1, null, 0, 0, rod, 0, PATH_ROD_SIZE.toInt())
    if (result != 0 || rw.getByte(0).toInt() == 0) return null
    return rod.getInt(SMOOTHED_RTT_OFFSET).takeIf { it > 0 }
}

/** Порт из записи таблицы: младшие два байта в сетевом порядке. */
private fun networkPort(raw: Int): Int = ((raw and 0xFF) shl 8) or ((raw shr 8) and 0xFF)

private const val AF_INET = 2
private const val TCP_TABLE_OWNER_PID_ALL = 5
private const val STATE_ESTABLISHED = 5
private const val ROW_FIELDS = 6
private const val ROW_SIZE = ROW_FIELDS * 4
private const val MIB_TCPROW_FIELDS = 5
private const val REMOTE_ADDRESS_OFFSET = 12L
private const val IPV4_BYTES = 4

/** `TcpConnectionEstatsPath`. */
private const val ESTATS_PATH = 3

/** Размер `TCP_ESTATS_PATH_ROD_v0`: 40 полей по 4 байта. */
private const val PATH_ROD_SIZE = 160L

/** Смещение `SmoothedRtt` в `TCP_ESTATS_PATH_ROD_v0`: 28-е поле. */
private const val SMOOTHED_RTT_OFFSET = 27L * 4
