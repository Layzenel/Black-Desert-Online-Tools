package com.layzen.bdotools.feature.comboassist.data.rage

import com.layzen.bdotools.composable.overlay.isGameForeground
import com.layzen.bdotools.feature.comboassist.data.history.captureScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.awt.Robot
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.time.TimeSource

/** Как часто искать бафы гнева. */
private const val CHECK_MS = 300L

/** Раз во сколько проверок писать в журнал счета, даже если ничего не поменялось. */
private const val LOG_EVERY = 30

/**
 * Висят ли бафы гнева Чёрного духа, пока поток собирают: раз в [CHECK_MS] снимает область вокруг «Списка усилений»
 * (место — [place], файл аккаунта) и ищет в ней три иконки подряд ([RageBuffMatcher]) с таймером «59 с.»
 * ([RageTimerReader]) — начало гнева; дальше гнев активен минуту ([RageClock]). Пока игра не на переднем плане или места
 * списка нет, экран не снимается. Смены и найденное раз в ≈10 с пишутся в журнал [log] (`rage.log`), если он есть:
 * сколько мест у каждой иконки, лучший счёт и расстояние таймеров до «59 с.».
 */
internal fun rageBuffs(place: () -> BuffListPlace?, log: Path?): Flow<Boolean> = flow {
    val robot = Robot()
    val clock = RageClock()
    val start = TimeSource.Monotonic.markNow()
    var geometry: Pair<BuffListPlace, RageBuffGeometry>? = null
    var matcher: RageBuffMatcher? = null
    var reader: RageTimerReader? = null
    var checks = 0
    log?.let { runCatching { Files.deleteIfExists(it) } }
    while (true) {
        val now = start.elapsedNow().inWholeMilliseconds
        val current = place()
        var started = false
        // экран снимается, только пока игра на переднем плане; минутный таймер идёт и без этого
        if (current != null && isGameForeground()) {
            if (geometry?.first != current) {
                geometry = current to RageBuffGeometry.forScreen(current)
                matcher = RageBuffMatcher(geometry.second.iconSize)
                reader = RageTimerReader(geometry.second.iconSize)
                log?.append(now, "geometry ${geometry.second}")
            }
            val area = geometry.second
            val shot = captureScreen(robot, area.logicalArea, area.area.width, area.area.height)
            val search = matcher!!.search(shot)
            val triple = search.triple
            val timers = triple?.map { reader!!.distance(shot, it) }
            started = timers != null && triple.all { reader!!.readsStart(shot, it) }
            if (started && !clock.active || ++checks % LOG_EVERY == 0) {
                val timerText = timers?.joinToString(" ") { "%.2f".format(it) } ?: "-"
                log?.append(now, "places $search, triple ${triple != null}, timers to 59 s: $timerText, started $started")
            }
        }
        if (clock.onCheck(now, started)) {
            log?.append(now, if (clock.active) "rage for 59 s" else "no rage")
            emit(clock.active)
        }
        delay(CHECK_MS)
    }
}.distinctUntilChanged().flowOn(Dispatchers.IO)

private fun Path.append(ms: Long, text: String) = runCatching {
    Files.createDirectories(parent)
    Files.writeString(this, "${ms.toString().padStart(TIME_WIDTH)} $text\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
}

private const val TIME_WIDTH = 7
