package com.layzen.bdotools.feature.comboassist.domain.skill

private val TAG = Regex("<[^>]*>")
private val RANK = Regex("""\s+[IVX]+$""")

// «Мощь:» и «Абсолют:» — варианты одного навыка: в командах других навыков они упоминаются без приставки
private val VARIANT_PREFIX = Regex("""^(Мощь|Абсолют|Prime|Absolute):\s*""")

/** Название навыка так, как его пишут команды других навыков: без разметки, «Мощь:», «Абсолют:» и ранга. */
fun commandName(name: String): String = name.replace(TAG, "").trim().replace(VARIANT_PREFIX, "").replace(RANK, "")

/**
 * Строка команды навыка, которой его жать после навыка с названием [previousName]: та, где он упомянут
 * («Во время навыка Алый коготь: {KeyBind:Attack2}»), иначе первая строка с клавишами. Пусто, если клавиш нет.
 */
fun commandLineFor(command: String, previousName: String?): String {
    val lines = command.split('\n').filter { "{KeyBind:" in it }
    val previous = previousName?.let(::commandName)?.takeIf { it.isNotEmpty() }
    return previous?.let { name -> lines.firstOrNull { name in it.replace(TAG, "") } } ?: lines.firstOrNull().orEmpty()
}
