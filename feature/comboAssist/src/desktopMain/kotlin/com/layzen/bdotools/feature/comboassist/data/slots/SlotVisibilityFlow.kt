package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.composable.overlay.isGameForeground
import com.layzen.bdotools.feature.comboassist.data.history.IconMatcher
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

/** Как часто проверять, видны ли слоты. */
private const val CHECK_MS = 100L

/** Раз во сколько проверок писать в журнал сводку, даже если видимость не менялась. */
private const val LOG_EVERY = 50

/**
 * Какие слоты перезарядок не видны (номера по порядку раскладки), пока поток собирают: раз в [CHECK_MS] снимает
 * слоты раскладки [layout] (последний узнанный персонаж) и узнаёт каждый по иконкам умений [icons] ([SlotVisibility]).
 * Сравнение — своё: кэш [IconMatcher] не рассчитан на два потока. Пока игра не на переднем плане, видимость не
 * меняется; без персонажа скрытых слотов нет. Смены и сводка раз в 5 с пишутся в журнал [log] (`slots.log`), если он есть.
 */
internal fun slotVisibility(layout: () -> SlotLayout?, icons: () -> Map<Int, ByteArray>, log: Path?): Flow<Set<Int>> = flow {
    val robot = Robot()
    val start = TimeSource.Monotonic.markNow()
    var geometry: Triple<SlotLayout, SlotGeometry?, SlotVisibility>? = null
    var matcher: Pair<Map<Int, ByteArray>, IconMatcher>? = null
    var checks = 0
    var reported: Set<Int>? = null
    log?.let { runCatching { Files.deleteIfExists(it) } }
    while (true) {
        val current = layout()
        if (current != null && isGameForeground()) {
            if (geometry?.first != current) geometry = Triple(current, SlotGeometry.forScreen(current), SlotVisibility(current.slots.size))
            val known = icons()
            if (matcher?.first !== known) matcher = known to IconMatcher(known, emptySet())
            geometry.second?.let { slots ->
                val shot = captureScreen(robot, slots.logicalArea, slots.area.width, slots.area.height)
                geometry.third.onCheck(recognizedSlots(current, matcher.second) { slots.crop(shot, it.centerX, it.centerY) })
                val hidden = geometry.third.hidden
                if (hidden != reported || ++checks % LOG_EVERY == 0) {
                    log?.append(start.elapsedNow().inWholeMilliseconds, "slots ${current.slots.size}, hidden ${hidden.sorted()}")
                    reported = hidden
                }
            }
        }
        emit(if (current == null) emptySet() else geometry?.third?.hidden.orEmpty())
        delay(CHECK_MS)
    }
}.distinctUntilChanged().flowOn(Dispatchers.IO)

/** Узнаётся ли каждый слот раскладки [layout] по картинке слота [crop] ([SlotVisibility.recognized]). */
private fun recognizedSlots(layout: SlotLayout, matcher: IconMatcher, crop: (CooldownSlot) -> java.awt.image.BufferedImage): List<Boolean> {
    val skills = layout.slots.map { it.skillNo }.distinct()
    return layout.slots.map { slot ->
        val image = crop(slot)
        val others = skills.filter { it != slot.skillNo }.mapNotNull { matcher.difference(image, it) }
        SlotVisibility.recognized(matcher.difference(image, slot.skillNo), others)
    }
}

/** Строка журнала с моментом [ms]; ошибки записи проглатываются: журнал не должен мешать проверке. */
private fun Path.append(ms: Long, text: String) {
    runCatching {
        Files.createDirectories(parent)
        Files.writeString(this, "%7d %s%n".format(ms, text), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }
}
