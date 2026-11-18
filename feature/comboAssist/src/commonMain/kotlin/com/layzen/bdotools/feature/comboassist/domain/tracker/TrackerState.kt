package com.layzen.bdotools.feature.comboassist.domain.tracker

/**
 * Где игрок в комбо. Меняет его только [ComboTracker]; остальные читают.
 *
 * @property positions шаги, на которых игрок может быть сейчас (обычно один); пусто — комбо ещё не начато
 * @property history последние три применённых умения, как в истории умений игры
 * @property done шаги, пройденные в этом проходе
 * @property burned шаги, которые в этом проходе уже не выйдут: сорванная связка или пропуск после ошибки
 * @property pass номер прохода с единицы
 * @property offCount умений не из комбо подряд
 * @property readyAt когда умение откатится (мс), по номеру навыка
 * @property forced шаг, который подсказывается следующим вместо обычного: повтор связки с запуска
 * @property rescue вход в оборванную связку через другое умение
 * @property lastUseMs когда было последнее применённое умение
 * @property lane ветка развилки (строка), по которой игрок шёл последней: при неоднозначности трекер держится её
 * @property forkLanes ветки развилок, пройденные в этом проходе (колонка → ветка): по ним идут связанные развилки
 */
class TrackerState {
    var positions: List<Int> = emptyList(); internal set
    var history: List<Int> = emptyList(); internal set
    val done: MutableSet<Int> = mutableSetOf()
    val burned: MutableSet<Int> = mutableSetOf()
    var pass: Int = 1; internal set
    var offCount: Int = 0; internal set
    val readyAt: MutableMap<Int, Long> = mutableMapOf()
    var forced: Int? = null; internal set
    var rescue: Rescue? = null; internal set
    var lastUseMs: Long? = null; internal set
    var lane: Int? = null; internal set
    val forkLanes: MutableMap<Int, Int> = mutableMapOf()

    /** Комбо ещё не начато или начато заново. */
    val atStart: Boolean get() = positions.isEmpty()

    /** Сколько ещё ждать умение [skillNo] в момент [nowMs]; 0 — готово. */
    fun cooldownLeft(skillNo: Int, nowMs: Long): Long = ((readyAt[skillNo] ?: 0) - nowMs).coerceAtLeast(0)

    /**
     * Вход в оборванную связку.
     *
     * @property entry умение-вход, которого нет в комбо на этом месте
     * @property target шаг связки, который выйдет после входа
     * @property armed вход уже применён, ждём [target]
     */
    data class Rescue(val entry: Int, val target: Int, val armed: Boolean = false)
}
