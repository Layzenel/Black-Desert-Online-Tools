package com.layzen.bdotools.feature.slots.domain

import kotlin.math.abs
import kotlin.math.roundToLong

/** Столько знаков после точки игра пишет в `RelativePosX/Y`. */
private const val RELATIVE_DIGITS = 10

/**
 * Заменяет в тексте `gamevariable.xml` аккаунта видимость и координаты у записей [entries]. Меняются только
 * значения `IsShow`, `PosX`, `PosY`, `RelativePosX`, `RelativePosY` внутри нужных тегов, остальной текст —
 * байт в байт прежний, включая переводы строк CRLF и другие атрибуты вроде `IsPinHold`.
 */
fun writeUiData(text: String, entries: Collection<UiData>): String =
    entries.fold(text) { current, entry ->
        current.replaceTag(entry.index) { tag ->
            tag.withAttribute("IsShow", entry.isShow.toString())
                .withAttribute("PosX", entry.posX.toString())
                .withAttribute("PosY", entry.posY.toString())
                .withAttribute("RelativePosX", entry.relativeX.toFixed(RELATIVE_DIGITS))
                .withAttribute("RelativePosY", entry.relativeY.toFixed(RELATIVE_DIGITS))
        }
    }

/**
 * Заменяет `IsShow` в файле настроек персонажа (`UserCache\<аккаунт>\11\<персонаж>\gamevariable.xml`).
 * Видимость игра хранит отдельно для каждого персонажа, а позиции — одни на аккаунт.
 * [visibility] — `Index` → показывать ли элемент. Записей, которых нет в файле, это не добавляет.
 */
fun writeVisibility(text: String, visibility: Map<Int, Boolean>): String =
    visibility.entries.fold(text) { current, (index, visible) ->
        current.replaceTag(index) { it.withAttribute("IsShow", visible.toString()) }
    }

private fun String.replaceTag(index: Int, change: (String) -> String): String =
    Regex("""<UIData\s+Index="$index"[^>]*>""").replace(this) { change(it.value) }

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
