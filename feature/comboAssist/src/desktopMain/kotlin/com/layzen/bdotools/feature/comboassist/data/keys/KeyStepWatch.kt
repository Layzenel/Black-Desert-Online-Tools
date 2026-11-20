package com.layzen.bdotools.feature.comboassist.data.keys

import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.feature.comboassist.data.history.WatchLog
import com.layzen.bdotools.feature.comboassist.data.resource.ResourceDrops
import com.layzen.bdotools.feature.comboassist.data.slots.HistoryMerge
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyStep
import com.layzen.bdotools.feature.comboassist.domain.keys.matchHeldStep
import com.layzen.bdotools.feature.comboassist.domain.keys.matchKeyStep
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Нажатия клавиш для слежения за игрой: [RawInputListener] складывает их в очередь, а цикл слежения разбирает
 * на каждом кадре ([onFrame]) — действие игры по раскладке персонажа ([ActionPresses]), ожидаемый шаг
 * ([matchKeyStep]) и подтверждение в [HistoryMerge]. Закрыть — перестать слушать.
 *
 * @property log журнал слежения: удалось ли слушать ввод и какие шаги подтвердили клавиши
 */
internal class KeyStepWatch(private val log: WatchLog) : AutoCloseable {
    private val queue = ConcurrentLinkedQueue<RawKey>()
    private val presses = ActionPresses()

    /** Когда последний раз нажали клавиши умения (засчитано оно или нет). */
    private val pressedAt = mutableMapOf<Int, Long>()

    @Volatile private var started: Boolean? = null
    private var reported = false
    private var failureReported = false
    private var firstFrameMs: Long? = null
    private val listener = RawInputListener({ started = it }, queue::add)

    /** Когда началось последнее умение, засчитанное клавишами (проседание ресурса или нажатие дешёвого), или `null`. */
    private var lastStartMs: Long? = null

    /** Нажатия умений, которые тратят ресурс, по порядку: ждут, пока умение начнётся и полоска ресурса просядет. */
    private val waiting = mutableListOf<Pair<KeyStep, Long>>()

    /**
     * Разбирает нажатия с прошлого кадра в момент [nowMs]: при раскладке [keys] нажатое действие — намерение сделать
     * шаг из [steps]. Само нажатие шаг не засчитывает: игроки жмут следующее умение заранее, во время анимации
     * предыдущего, и оно начинается позже. Шаг засчитывается в [merge], когда умение началось — ресурс просел
     * ([resource] — замеры полоски; `null`, пока её не нашли). Дешёвое умение ([KeyStep.visibleSpend] — `false`) и любое,
     * пока полоска не найдена, засчитывается сразу по нажатию: его расход не отличить от дрожания замера, а ошибку
     * поправит строка истории. Пока игра не на переднем плане
     * ([active] — `false`) или персонаж не узнан ([keys] — `null`), нажатия только отмечаются как зажатые.
     */
    fun onFrame(nowMs: Long, active: Boolean, keys: KeyBindings?, steps: () -> List<KeyStep>, merge: HistoryMerge, resource: ResourceDrops?) {
        reportStart(nowMs)
        while (true) {
            val key = queue.poll() ?: break
            val pressed = presses.onKey(key, keys ?: KeyBindings(emptyMap()), nowMs)
            if (!active) continue
            pressed.forEach { (action, held) -> match(nowMs, action, held, steps())?.let { step -> intend(nowMs, step, merge, resource) } }
        }
        resource?.let { confirmWaiting(nowMs, merge, it) }
        if (active && keys != null) confirmHeld(nowMs, keys, steps, merge, resource)
    }

    /**
     * Пока команда шага зажата, игра сама применяет умения на неё подряд (держишь ЛКМ — три умения), а нажатие было
     * одно. Свежее проседание, которое не забрало ни одно нажатие, достаётся шагу, которого ждут сейчас, если его
     * команда целиком зажата — и зажата ещё до прошлого засчитанного шага (только что нажатые клавиши — это новое
     * нажатие, его засчитает [confirmWaiting]). Проседание раньше [CHAIN_GAP_MS] после прошлого шага — ещё его расход.
     * Дешёвое умение проседания не даёт: оно засчитывается, когда команда продержалась [HELD_STEP_MS] после прошлого шага.
     */
    private fun confirmHeld(nowMs: Long, keys: KeyBindings, steps: () -> List<KeyStep>, merge: HistoryMerge, resource: ResourceDrops?) {
        val previous = lastStartMs ?: return
        val held = presses.heldActions(keys, downBefore = previous)
        if (held.isEmpty()) return
        val step = matchHeldStep(steps().filter { waiting.none { w -> w.first.skillNo == it.skillNo } }, held) ?: return
        val keysText = held.sorted().joinToString("+")
        if (step.visibleSpend && resource != null) {
            val drop = resource.claimFresh(nowMs, notBefore = previous + CHAIN_GAP_MS) ?: return
            log.line(nowMs, "skill ${step.skillNo}: started while $keysText held, resource spent at $drop")
            if (merge.onKey(step.skillNo, step.after, nowMs)) lastStartMs = drop
        } else if (nowMs - previous >= HELD_STEP_MS) {
            log.line(nowMs, "skill ${step.skillNo}: $keysText held after previous step")
            if (merge.onKey(step.skillNo, step.after, nowMs)) lastStartMs = nowMs
        }
    }

    override fun close() = listener.close()

    /**
     * Шаг, который подтверждает нажатие. Продолжение ([KeyStep.after]) — только если клавиши его запуска нажаты раньше:
     * иначе ЛКМ, зажатая ещё на прошлом шаге, засчитала бы продолжение умения, которое даже не применяли.
     */
    private fun match(nowMs: Long, action: String, held: Set<String>, steps: List<KeyStep>): KeyStep? {
        val allowed = steps.filter { step -> step.after == null || pressedAt[step.after]?.let { nowMs - it < LAUNCH_MS } == true }
        val step = matchKeyStep(allowed, action, held) ?: return null
        pressedAt[step.skillNo] = nowMs
        log.line(nowMs, "key $action (held ${held.sorted().joinToString("+")}) → skill ${step.skillNo}" + (step.after?.let { " after $it" } ?: ""))
        return step
    }

    /**
     * Намерение сделать шаг [step]: ждёт проседания ресурса, если расход заметен и полоска найдена; иначе засчитывается сразу.
     */
    private fun intend(nowMs: Long, step: KeyStep, merge: HistoryMerge, resource: ResourceDrops?) {
        if (resource == null || !step.visibleSpend) {
            if (merge.onKey(step.skillNo, step.after, nowMs)) {
                lastStartMs = nowMs
                log.line(nowMs, "skill ${step.skillNo}: counted on press (no visible spend)")
            }
            return
        }
        // повторное нажатие того же шага, пока первое ждёт, — та же попытка
        if (waiting.none { it.first.skillNo == step.skillNo }) waiting += step to nowMs
    }

    /**
     * Намерения по порядку нажатий забирают проседания ресурса ([ResourceDrops.claim]): одно проседание — один шаг,
     * засчитанный в момент проседания. Не дождалось за [HOLD_MS] — умение так и не вышло.
     */
    private fun confirmWaiting(nowMs: Long, merge: HistoryMerge, resource: ResourceDrops) {
        val done = mutableListOf<Pair<KeyStep, Long>>()
        for (intent in waiting) {
            val (step, at) = intent
            val drop = resource.claim(at)
            if (drop != null) {
                log.line(nowMs, "skill ${step.skillNo}: started, resource spent at $drop (pressed at $at)")
                if (merge.onKey(step.skillNo, step.after, nowMs)) lastStartMs = drop
                done += intent
            } else if (nowMs - at > HOLD_MS) {
                log.line(nowMs, "skill ${step.skillNo}: no resource spent, not counted; fills ${resource.trace(at - TRACE_BEFORE_MS, nowMs)}")
                done += intent
            }
        }
        waiting.removeAll(done)
    }

    /** Пишет в журнал, слушается ли ввод: запустился, не смог, не ответил за [SILENT_MS] или упал уже после запуска. */
    private fun reportStart(nowMs: Long) {
        val firstFrame = firstFrameMs ?: nowMs.also { firstFrameMs = it }
        val failure = listener.failure
        if (failure != null && !failureReported && reported) { failureReported = true; log.line(nowMs, "key input: stopped, $failure") }
        if (reported) return
        val ok = started
        if (ok == null) {
            if (nowMs - firstFrame > SILENT_MS) { reported = true; log.line(nowMs, "key input: listener did not answer" + (failure?.let { ", $it" } ?: "")) }
            return
        }
        reported = true
        failureReported = failure != null
        log.line(nowMs, if (ok) "key input: listening" else "key input: not available" + (failure?.let { ", $it" } ?: ""))
    }

    private companion object {
        /** Сколько после нажатия клавиш запуска ждать нажатия продолжения. */
        const val LAUNCH_MS = 2000L

        /**
         * Сколько нажатие ждёт, пока умение начнётся: его жмут заранее, во время анимации предыдущего умения.
         * shortcut: догадка о самой длинной анимации; уточнить по «resource spent at … (pressed at …)» в `watch.log`.
         */
        const val HOLD_MS = 2500L

        /** Сколько замеров до нажатия показать в журнале, когда расхода не нашлось. */
        const val TRACE_BEFORE_MS = 300L

        /**
         * Проседание раньше этого после прошлого шага — ещё расход того же умения, а не следующее в цепочке
         * (2026-11-20: через 0,17 с после 7988 «зажатая ПКМ» засчитала 7984, а он начался через секунду). Связки
         * Страйкера идут быстро, поэтому не больше 0,25 с.
         */
        const val CHAIN_GAP_MS = 250L

        /**
         * Сколько команда дешёвого умения должна продержаться после прошлого шага, чтобы засчитать следующий на той же
         * клавише (Страйкер: ЛКМ держат — 2549, затем 2550). shortcut: догадка о длине анимации связки; уточнить по `watch.log`.
         */
        const val HELD_STEP_MS = 600L

        /** Слушатель не ответил за это время — в журнал: поток мог зависнуть, ничего не сообщив. */
        const val SILENT_MS = 3000L
    }
}
