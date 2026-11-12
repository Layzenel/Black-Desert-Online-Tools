package com.layzen.bdotools.feature.uieditor.domain.skill

private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL = 0xFF

/**
 * Связки клавиш из блока «Команда» тултипа навыка [command]: в каждой строке — только зелёные куски (так игра
 * выделяет клавиши: «S + F» из «S + F», «F» из «Для заряда: удерживать F»). Строки без зелёного пропускаются,
 * порядок — как в тултипе. Подпись клавиш (`{KeyBind:…}`) подставляет отображение.
 */
fun commandActions(command: String): List<List<SkillTextPart>> =
    command.split('\n')
        .map { line -> parseSkillMarkup(line).filter { part -> part.color?.let(::isGreen) == true } }
        .filter { parts -> parts.any { it is SkillTextPart.Key || it is SkillTextPart.Plain && it.text.isNotBlank() } }
        .distinct()

/** Зелёный (`#D8FF00` и похожие): зелёный канал сильнее красного и синего. */
private fun isGreen(rgb: Int): Boolean {
    val red = rgb shr RED_SHIFT and CHANNEL
    val green = rgb shr GREEN_SHIFT and CHANNEL
    val blue = rgb and CHANNEL
    return green > red && green > blue
}
