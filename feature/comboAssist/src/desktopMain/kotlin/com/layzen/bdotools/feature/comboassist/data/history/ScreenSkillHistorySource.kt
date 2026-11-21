package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.comboassist.data.keys.KeyStepWatch
import com.layzen.bdotools.feature.comboassist.data.rage.BuffListPlace
import com.layzen.bdotools.feature.comboassist.data.resource.ResourceWatch
import com.layzen.bdotools.feature.comboassist.data.rage.rageBuffs
import com.layzen.bdotools.feature.comboassist.data.slots.CooldownSlotWatch
import com.layzen.bdotools.feature.comboassist.data.slots.HistoryMerge
import com.layzen.bdotools.feature.comboassist.data.slots.PlayedCharacterDetector
import com.layzen.bdotools.feature.comboassist.data.slots.SlotLayout
import com.layzen.bdotools.feature.comboassist.data.slots.SlotLayoutFiles
import com.layzen.bdotools.feature.comboassist.data.slots.slotVisibility
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.history.SkillHistorySource
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyStep
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
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
import java.awt.image.BufferedImage
import kotlin.time.TimeSource

/**
 * Следит за «Историей испол. навыков» на экране: 20 раз в секунду снимает нижнюю и среднюю строки в настоящем
 * разрешении экрана (без уменьшения от масштаба Windows). Когда открывается новая строка ([HistoryLines]),
 * иконка сравнивается на каждом кадре, пока не узнана ([IconMatcher]).
 *
 * Умение, которого игра не записала в историю, дописывает слот перезарядки, ушедший на перезарядку
 * ([CooldownSlotWatch]), а умение, которого нет ни в истории, ни на слотах (флаг в редакторе), — его клавиши
 * ([KeyStepWatch]) и расход ресурса ([ResourceWatch]). Чему верить первым — в [HistoryMerge].
 * Ход работы — в [WatchLog].
 *
 * shortcut: область истории — замер 2026-11-13 (2560×1440, «Размер интерфейса» 80 %, элемент у левого края над чатом),
 * пересчитанный по высоте экрана; когда понадобится другая позиция — брать `UIData Index="158"` из
 * `gamevariable.xml` выбранного аккаунта и дать игроку подвинуть рамку.
 */
internal class ScreenSkillHistorySource : SkillHistorySource {
    override val supported: Boolean = true

    /** Слоты персонажа, последнего узнанного в [playedSlots]: по ним проверяется, видны ли слоты. */
    @Volatile private var played: SlotLayout? = null

    /** Иконки умений из слотов для другого потока: [slotVisibility] сравнивает их своим [IconMatcher]. */
    @Volatile private var slotIconsShared: Map<Int, ByteArray> = emptyMap()

    override fun hiddenSlots(): Flow<Set<Int>> {
        val log = GameFolderSettings().folder?.resolve("BDO Tools")?.resolve("comboAssist")?.resolve("slots.log")
        return slotVisibility({ played }, { slotIconsShared }, log)
    }

    override fun rageActive(): Flow<Boolean> {
        val settings = GameFolderSettings()
        val folder = settings.folder
        // файл аккаунта читается один раз и уже в потоке слежения: он большой
        val place by lazy {
            folder?.let { SlotLayoutFiles.accountDir(it, settings.account) }?.let(SlotLayoutFiles::accountText)?.let(BuffListPlace::parse)
        }
        return rageBuffs({ place }, folder?.resolve("BDO Tools")?.resolve("comboAssist")?.resolve("rage.log"))
    }

    /** Иконки умений из слотов всех персонажей для [playedSlots]. */
    private val slotIcons = mutableMapOf<Int, ByteArray>()
    private val triedIcons = mutableSetOf<Int>()

    /** Сравнение со слотами; создаётся заново, только когда добавились иконки, — так уменьшенные иконки не пересчитываются. */
    private var slotMatcher = IconMatcher(emptyMap(), emptySet())

    override fun watch(
        icons: Map<Int, ByteArray>,
        preferred: Set<Int>,
        keySteps: () -> List<KeyStep>,
    ): Flow<Int> = flow {
        val settings = GameFolderSettings()
        val folder = settings.folder
        val log = WatchLog(folder?.resolve("BDO Tools")?.resolve("comboAssist")?.resolve("watch.log"))
        val matcher = IconMatcher(icons, preferred)
        val lines = HistoryLines()
        val robot = Robot()
        val start = TimeSource.Monotonic.markNow()
        val geometry = HistoryGeometry.forScreen()
        val accountDir = folder?.let { SlotLayoutFiles.accountDir(it, settings.account) }
        val layouts = accountDir?.let(SlotLayoutFiles::readAll).orEmpty()
        val slots = CooldownSlotWatch(layouts, matcher, log)
        val merge = HistoryMerge()
        val keys = KeyStepWatch(log)
        val accountText = accountDir?.let(SlotLayoutFiles::accountText)
        val resource = ResourceWatch.of(accountText, log)
        currentCoroutineContext().job.invokeOnCompletion { keys.close() }
        log.line(0, "start: $geometry, icons ${icons.size}, combo ${preferred.size}, characters with cooldown slots ${layouts.size}")
        var gameActive = false
        // лучший кадр открытой и ещё не узнанной строки: в журнал вместе с картинкой, если узнать не выйдет
        var miss: Pair<IconMatch, BufferedImage>? = null
        var checkedAt = -FOREGROUND_CHECK_MS
        while (true) {
            val now = start.elapsedNow().inWholeMilliseconds
            // игра не на переднем плане — экран не снимается: свёрнутая или в другом окне игра ничего не применяет
            if (now - checkedAt >= FOREGROUND_CHECK_MS) {
                gameActive = isGameForeground()
                checkedAt = now
            }
            if (!gameActive) {
                keys.onFrame(now, active = false, played?.keys, keySteps, merge, resource = null)
                delay(PAUSED_MS)
                continue
            }
            val frame = captureScreen(robot, geometry.logicalRect, geometry.width, geometry.height)
            val bottom = geometry.textPixels(frame, geometry.bottomBand) > geometry.minTextPixels
            val middle = geometry.textPixels(frame, geometry.middleBand) > geometry.minTextPixels
            when (val step = lines.onFrame(bottom, middle, now)) {
                is HistoryLines.Step.Try -> {
                    val icon = geometry.icon(frame)
                    val match = matcher.match(icon)
                    if (step.shift) log.line(now, "line opened by shift")
                    if (match.accepted) {
                        lines.recognized()
                        miss = null
                        log.line(now, "skill ${match.skillNo} score %.1f second %.1f".format(match.score, match.second))
                        emitAll(merge.onHistory(match.skillNo!!, now), now, log, history = match.skillNo)
                    } else if (match.score < (miss?.first?.score ?: Double.MAX_VALUE)) {
                        // самый похожий кадр строки — в журнал, если строку так и не узнают
                        miss = match to icon
                    }
                }
                HistoryLines.Step.GaveUp -> {
                    val best = miss
                    log.line(now, "not recognized" + (best?.first?.let { ", best ${it.skillNo} score %.1f second %.1f".format(it.score, it.second) } ?: ""))
                    best?.let { log.miss(now, it.second) }
                    miss = null
                }
                HistoryLines.Step.Idle -> Unit
            }
            slots.onFrame(robot, now).forEach { skillNo ->
                log.line(now, "slot $skillNo on cooldown")
                merge.onSlot(skillNo, now)
            }
            val drops = resource.onFrame(robot, now)
            keys.onFrame(now, active = true, played?.keys, keySteps, merge, drops)
            emitAll(merge.onTick(now), now, log, history = null)
            delay(FRAME_MS)
        }
    }.flowOn(Dispatchers.IO)

    /** Выпускает умения [skills]; все, кроме узнанного в истории [history], дописаны слотом или клавишами — это видно в журнале. */
    private suspend fun FlowCollector<Int>.emitAll(skills: List<Int>, now: Long, log: WatchLog, history: Int?) {
        skills.forEach { skillNo ->
            if (skillNo != history) log.line(now, "skill $skillNo added without history (slot or keys)")
            emit(skillNo)
        }
    }

    override fun historyShown(): Boolean = played?.historyShown ?: true

    override suspend fun playedSlots(icon: suspend (Int) -> ByteArray?): List<ScreenSlot>? = withContext(Dispatchers.IO) {
        // игра не на переднем плане — программа за ней не следит: ни снимков экрана, ни чтения файлов, ни загрузки иконок
        if (!isGameForeground()) return@withContext null
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
        if (missing.isNotEmpty()) {
            slotIconsShared = slotIcons.toMap()
            slotMatcher = IconMatcher(slotIconsShared, emptySet())
        }
        // иконки могли качаться долго: пока качались, игру могли свернуть
        if (!isGameForeground()) return@withContext null
        val layout = PlayedCharacterDetector.detect(Robot(), layouts, slotMatcher) ?: return@withContext null
        played = layout
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
