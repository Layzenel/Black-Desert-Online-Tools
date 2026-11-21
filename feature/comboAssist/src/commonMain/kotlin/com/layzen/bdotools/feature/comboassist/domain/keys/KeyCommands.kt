package com.layzen.bdotools.feature.comboassist.domain.keys

private val KEY_BIND = Regex("""\{KeyBind:(\w+)\}""")
private val ALTERNATIVE = Regex("""\s+(?:or|или)\s+""")

/** Часть команды о том, что делает клавиша во время умения: «Hold {KeyBind:Attack2} to fire continuously». */
private val PURPOSE = Regex("""\}(?:</span>)?\s+to\s""")

/**
 * Варианты клавиш команды навыка [command]: каждая строка и каждая её часть через «or»/«или» — отдельный вариант,
 * в нём действия, зажатые вместе («{KeyBind:Dash} + {KeyBind:Attack1}» → `Dash`, `Attack1`). Условия строки
 * («после навыка …») не разбираются: когда умение можно жать, решает трекер. Строки без клавиш пропускаются.
 */
fun keyVariants(command: String): List<Set<String>> {
    val parts = command.split('\n').flatMap { line -> line.split(ALTERNATIVE) }
    fun variants(of: List<String>) = of.map { part -> KEY_BIND.findAll(part).map { it.groupValues[1] }.toSet() }.filter { it.isNotEmpty() }.distinct()
    // «Hold {RMB} to fire continuously» — что делать уже во время умения, а не как его начать (у 7984 Агента
    // это «просто ПКМ», которой начинается и умение перед ним); если других вариантов нет — оставить и её
    return variants(parts.filterNot { PURPOSE.containsMatchIn(it) }).ifEmpty { variants(parts) }
}

/**
 * Шаг из [steps], который подтверждает нажатие действия [pressed], пока зажаты [held] (вместе с [pressed]):
 * вариант команды содержит [pressed] и весь зажат. Шаг, которого ждут сейчас, важнее следующего за ним, а из
 * подходящих — самый точный вариант (W + ЛКМ точнее ЛКМ). Если два разных умения подходят одинаково — `null`:
 * угадывать нельзя.
 */
fun matchKeyStep(steps: List<KeyStep>, pressed: String, held: Set<String>): KeyStep? =
    best(steps.mapNotNull { step -> step.variants.filter { pressed in it && held.containsAll(it) }.maxOfOrNull { it.size }?.let { step to it } })

/**
 * Шаг из [steps], который ждут сейчас и чья команда целиком зажата ([held]): пока игрок держит клавишу, игра сама
 * применяет умения на неё подряд (зажатая ЛКМ — три умения). Самый точный вариант; два разных умения поровну — `null`.
 */
fun matchHeldStep(steps: List<KeyStep>, held: Set<String>): KeyStep? =
    best(steps.filter { it.after == null }.mapNotNull { step -> step.variants.filter { held.containsAll(it) }.maxOfOrNull { it.size }?.let { step to it } })

/** Лучший из подходящих шагов [matches] (шаг и размер подошедшего варианта): ждут сейчас, самый точный; ничья — `null`. */
private fun best(matches: List<Pair<KeyStep, Int>>): KeyStep? {
    val hits = matches.sortedWith(compareBy({ it.first.after != null }, { -it.second }))
    val best = hits.firstOrNull() ?: return null
    val tie = hits.drop(1).any { (step, size) ->
        step.after == best.first.after && size == best.second && step.skillNo != best.first.skillNo
    }
    return if (tie) null else best.first
}

/** Строка команды с разметкой клавиш для действий [actions], зажатых вместе: «{KeyBind:Dash} + {KeyBind:Attack1}». */
fun keyVariantLine(actions: Set<String>): String = actions.joinToString(" + ") { "{KeyBind:$it}" }
