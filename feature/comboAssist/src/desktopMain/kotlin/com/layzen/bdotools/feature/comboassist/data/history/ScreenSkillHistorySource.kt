package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.comboassist.data.slots.HistoryMerge
import com.layzen.bdotools.feature.comboassist.data.slots.PlayedCharacterDetector
import com.layzen.bdotools.feature.comboassist.data.slots.CooldownSlotWatch
import com.layzen.bdotools.feature.comboassist.data.slots.SlotLayoutFiles
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.history.SkillHistorySource
import com.layzen.bdotools.composable.overlay.isGameForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.awt.Robot
import kotlin.time.TimeSource

/**
 * Следит за «Историей испол. навыков» на экране: 20 раз в секунду снимает нижнюю и среднюю строки в настоящем
 * разрешении экрана (без уменьшения от масштаба Windows). Когда открывается новая строка ([HistoryLines]),
 * иконка сравнивается на каждом кадре, пока не узнана ([IconMatcher]).
 *
 * Заодно следит за слотами перезарядок персонажа ([SlotLayoutFiles]): слот, ушедший на перезарядку
 * ([SlotDarkening]), дописывает умение, которое игра не записала в историю ([HistoryMerge]). Ход работы — в [WatchLog].
 *
 * shortcut: область истории — замер 2026-11-13 (2560×1440, «Размер интерфейса» 80 %, элемент у левого края над чатом),
 * пересчитанный по высоте экрана; когда понадобится другая позиция — брать `UIData Index="158"` из
 * `gamevariable.xml` выбранного аккаунта и дать игроку подвинуть рамку.
 */
internal class ScreenSkillHistorySource : SkillHistorySource {
    override val supported: Boolean = true

    /** Иконки умений из слотов всех персонажей для [playedSlots]. */
    private val slotIcons = mutableMapOf<Int, ByteArray>()
    private val triedIcons = mutableSetOf<Int>()

    /** Сравнение со слотами; создаётся заново, только когда добавились иконки, — так уменьшенные иконки не пересчитываются. */
    private var slotMatcher = IconMatcher(emptyMap(), emptySet())

    override fun watch(icons: Map<Int, ByteArray>, preferred: Set<Int>): Flow<Int> = flow {
        val settings = GameFolderSettings()
        val folder = settings.folder
        val log = WatchLog(folder?.resolve("BDO Tools")?.resolve("comboAssist")?.resolve("watch.log"))
        val matcher = IconMatcher(icons, preferred)
        val lines = HistoryLines()
        val robot = Robot()
        val start = TimeSource.Monotonic.markNow()
        val geometry = HistoryGeometry.forScreen()
        val layouts = folder?.let { SlotLayoutFiles.accountDir(it, settings.account) }?.let(SlotLayoutFiles::readAll).orEmpty()
        val slots = CooldownSlotWatch(layouts, matcher, log)
        val merge = HistoryMerge()
        log.line(0, "start: $geometry, icons ${icons.size}, combo ${preferred.size}, characters with cooldown slots ${layouts.size}")
        var gameActive = false
        var checkedAt = -FOREGROUND_CHECK_MS
        while (true) {
            val now = start.elapsedNow().inWholeMilliseconds
            // игра не на переднем плане — экран не снимается: свёрнутая или в другом окне игра ничего не применяет
            if (now - checkedAt >= FOREGROUND_CHECK_MS) {
                gameActive = isGameForeground()
                checkedAt = now
            }
            if (!gameActive) {
                delay(PAUSED_MS)
                continue
            }
            val frame = captureScreen(robot, geometry.logicalRect, geometry.width, geometry.height)
            val bottom = geometry.textPixels(frame, geometry.bottomBand) > geometry.minTextPixels
            val middle = geometry.textPixels(frame, geometry.middleBand) > geometry.minTextPixels
            when (val step = lines.onFrame(bottom, middle, now)) {
                is HistoryLines.Step.Try -> {
                    val match = matcher.match(geometry.icon(frame))
                    if (step.shift) log.line(now, "line opened by shift")
                    if (match.accepted) {
                        lines.recognized()
                        log.line(now, "skill ${match.skillNo} score %.1f second %.1f".format(match.score, match.second))
                        emitAll(merge.onHistory(match.skillNo!!, now), now, log, history = match.skillNo)
                    }
                }
                HistoryLines.Step.GaveUp -> log.line(now, "not recognized")
                HistoryLines.Step.Idle -> Unit
            }
            slots.onFrame(robot, now).forEach { skillNo ->
                log.line(now, "slot $skillNo on cooldown")
                merge.onSlot(skillNo, now)
            }
            emitAll(merge.onTick(now), now, log, history = null)
            delay(FRAME_MS)
        }
    }.flowOn(Dispatchers.IO)

    /** Выпускает умения [skills]; все, кроме узнанного в истории [history], дописаны слотами — это видно в журнале. */
    private suspend fun FlowCollector<Int>.emitAll(skills: List<Int>, now: Long, log: WatchLog, history: Int?) {
        skills.forEach { skillNo ->
            if (skillNo != history) log.line(now, "skill $skillNo added from cooldown slot")
            emit(skillNo)
        }
    }

    override suspend fun playedSlots(icon: suspend (Int) -> ByteArray?): List<ScreenSlot>? = withContext(Dispatchers.IO) {
        val settings = GameFolderSettings()
        val layouts = settings.folder?.let { SlotLayoutFiles.accountDir(it, settings.account) }?.let(SlotLayoutFiles::readAll).orEmpty()
        val skills = layouts.flatMap { layout -> layout.slots.map { it.skillNo } }.toSet()
        // иконки скачиваются один раз и лежат в памяти: проверка повторяется, пока программа следит за игрой
        val missing = skills - triedIcons
        triedIcons += missing
        // параллельно: иконок у всех персонажей сотня, по одной первая проверка шла бы десятки секунд
        val limit = Semaphore(ICON_DOWNLOADS)
        coroutineScope { missing.map { no -> async { no to limit.withPermit { icon(no) } } }.awaitAll() }
            .forEach { (no, bytes) -> bytes?.let { slotIcons[no] = it } }
        if (missing.isNotEmpty()) slotMatcher = IconMatcher(slotIcons.toMap(), emptySet())
        // иконки готовятся заранее, а экран снимается, только когда игра на переднем плане
        if (!isGameForeground()) return@withContext null
        val layout = PlayedCharacterDetector.detect(Robot(), layouts, slotMatcher) ?: return@withContext null
        val size = SLOT_UNITS * layout.uiScale / layout.resolutionHeight
        layout.slots.map { ScreenSlot(it.skillNo, it.centerX, it.centerY, size) }
    }

    private companion object {
        const val FRAME_MS = 50L

        /** Сколько иконок слотов качать одновременно. */
        const val ICON_DOWNLOADS = 16

        /** Как часто проверять, что игра на переднем плане. */
        const val FOREGROUND_CHECK_MS = 300L

        /** Пауза между проверками, пока игра не на переднем плане. */
        const val PAUSED_MS = 300L

        /** Сторона слота перезарядки в единицах интерфейса игры. */
        const val SLOT_UNITS = 50.0
    }
}

internal actual fun createSkillHistorySource(): SkillHistorySource = ScreenSkillHistorySource()
