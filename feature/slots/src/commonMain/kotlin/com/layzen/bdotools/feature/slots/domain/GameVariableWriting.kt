package com.layzen.bdotools.feature.slots.domain

import kotlin.math.abs
import kotlin.math.roundToLong

/** Столько знаков после точки игра пишет в `RelativePosX/Y`. */
private const val RELATIVE_DIGITS = 10

/**
 * Заменяет в тексте `gamevariable.xml` координаты у записей [entries]. Меняются только значения
 * `PosX`, `PosY`, `RelativePosX`, `RelativePosY` внутри нужных тегов, остальной текст — байт в байт
 * прежний, включая переводы строк CRLF и другие атрибуты вроде `IsPinHold`.
 */
fun writeUiData(text: String, entries: Collection<UiData>): String =
    entries.fold(text) { current, entry ->
        val tag = Regex("""<UIData\s+Index="${entry.index}"[^>]*>""")
        tag.replace(current) { match -> withCoordinates(match.value, entry) }
    }

private fun withCoordinates(tag: String, entry: UiData): String = tag
    .withAttribute("PosX", entry.posX.toString())
    .withAttribute("PosY", entry.posY.toString())
    .withAttribute("RelativePosX", entry.relativeX.toFixed(RELATIVE_DIGITS))
    .withAttribute("RelativePosY", entry.relativeY.toFixed(RELATIVE_DIGITS))

private fun String.withAttribute(name: String, value: String): String =
    replace(Regex("""\b$name="[^"]*""""), "$name=\"$value\"")

/** Число с [digits] знаками после точки, без экспоненты: `0.3393750000`. */
internal fun Double.toFixed(digits: Int): String {
    var scale = 1L
    repeat(digits) { scale *= 10 }
    val scaled = abs(this * scale).roundToLong()
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    val fraction = (scaled % scale).toString().padStart(digits, '0')
    return "$sign${scaled / scale}.$fraction"
}
