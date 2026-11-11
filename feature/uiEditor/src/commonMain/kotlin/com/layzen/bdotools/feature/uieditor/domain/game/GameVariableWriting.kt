package com.layzen.bdotools.feature.uieditor.domain.game

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Заменяет в тексте `gamevariable.xml` аккаунта видимость и координаты у записей [entries] набора [preset].
 * Меняются только значения `IsShow`, `PosX`, `PosY`, `RelativePosX`, `RelativePosY` внутри тегов этого набора,
 * остальной текст — байт в байт прежний, включая переводы строк CRLF и другие атрибуты вроде `IsPinHold`.
 */
fun writeUiData(text: String, entries: Collection<UiData>, preset: GamePreset = GamePreset.General): String =
    entries.fold(text) { current, entry ->
        current.replaceTag(preset.tag, entry.index) { tag ->
            tag.withAttribute("IsShow", entry.isShow.toString())
                .withAttribute("PosX", entry.posX.toString())
                .withAttribute("PosY", entry.posY.toString())
                .withAttribute("RelativePosX", entry.relativeX.toFixed(preset.relativeDigits))
                .withAttribute("RelativePosY", entry.relativeY.toFixed(preset.relativeDigits))
        }
    }

/**
 * Заменяет `IsShow` в файле настроек персонажа (`UserCache\<аккаунт>\11\<персонаж>\gamevariable.xml`).
 * Видимость игра хранит отдельно для каждого персонажа, а позиции — одни на аккаунт.
 * [visibility] — `Index` → показывать ли элемент. Записей, которых нет в файле, это не добавляет.
 */
fun writeVisibility(text: String, visibility: Map<Int, Boolean>): String =
    visibility.entries.fold(text) { current, (index, visible) ->
        current.replaceTag(GamePreset.General.tag, index) { it.withAttribute("IsShow", visible.toString()) }
    }

/**
 * Записывает «Настройку быстрых слотов» в файл аккаунта. Если записи ещё нет, она добавляется первой в `<UIValue>`
 * с тем же переводом строки, что у файла; без `<UIValue>` текст не меняется.
 */
fun writeCustomQuickSlots(text: String, enabled: Boolean): String {
    if (CUSTOM_QUICK_SLOTS.containsMatchIn(text)) {
        return CUSTOM_QUICK_SLOTS.replace(text) { it.value.replace(it.groupValues[1], enabled.toString()) }
    }
    val newline = if ("\r\n" in text) "\r\n" else "\n"
    val end = (Regex("""<UIValue\s[^>]*>""").find(text) ?: return text).range.last + 1
    val entry = "$newline\t<Bool Index=\"1\" Value=\"$enabled\" StorageType=\"1\"/>"
    return text.substring(0, end) + entry + text.substring(end)
}

private fun String.replaceTag(tag: String, index: Int, change: (String) -> String): String =
    Regex("""<$tag\s+Index="$index"[^>]*>""").replace(this) { change(it.value) }

/** Тег с новым значением [value] у атрибута [name]; остальной текст тега не трогает. */
internal fun String.withAttribute(name: String, value: String): String =
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
